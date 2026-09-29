import streamlit as st
import pandas as pd
from elasticsearch import Elasticsearch


# =====================================================
# Elasticsearch connection
# =====================================================

@st.cache_resource
def get_es():
    return Elasticsearch("http://localhost:9200")


es = get_es()


# =====================================================
# Page config
# =====================================================

st.set_page_config(
    page_title="Product Faceted Search",
    page_icon="🔎",
    layout="wide"
)

st.title("🔎 Elasticsearch Faceted Search")
st.caption("Self-excluding facets with Streamlit")


# =====================================================
# Initial product lines
# =====================================================

@st.cache_data
def get_product_lines():

    response = es.search(
        index="products",
        size=0,
        aggs={
            "product_lines": {
                "terms": {
                    "field": "productline",
                    "size": 50
                }
            }
        }
    )

    return [
        bucket["key"]
        for bucket in response["aggregations"]["product_lines"]["buckets"]
    ]


product_lines = get_product_lines()


# =====================================================
# Sidebar
# =====================================================

with st.sidebar:

    st.header("Filters")

    keyword = st.text_input(
        "Search product",
        placeholder="mustang, ford, motorcycle..."
    )

    selected_lines = st.multiselect(
        "Product Line",
        product_lines
    )

    st.markdown("### Buy Price")

    price_range = st.slider(
        "Price range",
        min_value=0.0,
        max_value=200.0,
        value=(0.0, 200.0),
        step=1.0,
        label_visibility="collapsed"
    )

    st.write(
        f"${price_range[0]:.0f} — ${price_range[1]:.0f}"
    )

    st.divider()

    if st.button(
        "Clear Filters",
        use_container_width=True
    ):
        st.rerun()


# =====================================================
# Query
# =====================================================

if keyword:

    query = {
        "multi_match": {
            "query": keyword,
            "fields": [
                "productname",
                "productdescription"
            ]
        }
    }

else:

    query = {
        "match_all": {}
    }


# =====================================================
# Filters
# =====================================================

product_line_filter = None

if selected_lines:

    product_line_filter = {
        "terms": {
            "productline": selected_lines
        }
    }


price_filter = {
    "range": {
        "buyprice": {
            "gte": price_range[0],
            "lte": price_range[1]
        }
    }
}


# =====================================================
# post_filter
#
# Controls search results
# =====================================================

hit_filters = []

if product_line_filter:
    hit_filters.append(product_line_filter)

hit_filters.append(price_filter)


post_filter = {
    "bool": {
        "filter": hit_filters
    }
}


# =====================================================
# Aggregations
#
# Product Line facet ignores its own selection,
# but respects Price.
#
# Price facet ignores its own selection,
# but respects Product Line.
# =====================================================

aggregations = {

    "productline_facet": {

        "filter": price_filter,

        "aggs": {

            "values": {

                "terms": {
                    "field": "productline",
                    "size": 50
                }

            }

        }

    }

}


# =====================================================
# Price facet
# =====================================================

price_ranges = [

    {
        "key": "Under $50",
        "to": 50
    },

    {
        "key": "$50-$99.99",
        "from": 50,
        "to": 100
    },

    {
        "key": "$100-$199.99",
        "from": 100,
        "to": 200
    },

    {
        "key": "$200+",
        "from": 200
    }

]


if product_line_filter:

    price_facet_filter = product_line_filter

else:

    price_facet_filter = {
        "match_all": {}
    }


aggregations["price_facet"] = {

    "filter": price_facet_filter,

    "aggs": {

        "values": {

            "range": {
                "field": "buyprice",
                "ranges": price_ranges
            }

        }

    }

}


# =====================================================
# Execute search
# =====================================================

result = es.search(
    index="products",
    size=40,
    query=query,
    aggs=aggregations,
    post_filter=post_filter
)


# =====================================================
# Data preparation
# =====================================================

hits = result["hits"]["hits"]

total = result["hits"]["total"]["value"]


rows = []

for hit in hits:

    source = hit["_source"]

    rows.append({

        "Product Code":
            source.get("productcode"),

        "Product Name":
            source.get("productname"),

        "Product Line":
            source.get("productline"),

        "Buy Price":
            source.get("buyprice"),

        "MSRP":
            source.get("msrp"),

        "Score":
            hit.get("_score")

    })


df = pd.DataFrame(rows)


# =====================================================
# Facet data
# =====================================================

product_line_buckets = (
    result["aggregations"]
    ["productline_facet"]
    ["values"]
    ["buckets"]
)


product_line_facets = [

    {
        "Product Line": bucket["key"],
        "Count": bucket["doc_count"],
        "Selected": bucket["key"] in selected_lines
    }

    for bucket in product_line_buckets

]


price_buckets = (
    result["aggregations"]
    ["price_facet"]
    ["values"]
    ["buckets"]
)


price_facets = [

    {
        "Price Range": bucket["key"],
        "Count": bucket["doc_count"]
    }

    for bucket in price_buckets

]


# =====================================================
# Summary row
# =====================================================

summary1, summary2, summary3 = st.columns(3)


with summary1:

    st.metric(
        "Matched Products",
        total
    )


with summary2:

    st.metric(
        "Selected Product Lines",
        len(selected_lines)
    )


with summary3:

    st.metric(
        "Returned Rows",
        len(hits)
    )


st.divider()


# =====================================================
# Main tabs
# =====================================================

tab_results, tab_facets, tab_debug = st.tabs(
    [
        "Search Results",
        "Facet Details",
        "Elasticsearch Query"
    ]
)


# =====================================================
# Results tab
# =====================================================

with tab_results:

    st.subheader("Products")

    if df.empty:

        st.info("No products found.")

    else:

        st.dataframe(
            df,
            use_container_width=True,
            hide_index=True,
            column_config={

                "Buy Price": st.column_config.NumberColumn(
                    format="$%.2f"
                ),

                "MSRP": st.column_config.NumberColumn(
                    format="$%.2f"
                ),

                "Score": st.column_config.NumberColumn(
                    format="%.4f"
                )

            }
        )


# =====================================================
# Facets tab
# =====================================================

with tab_facets:

    facet_left, facet_right = st.columns(2)


    # -------------------------------------------------
    # Product Line
    # -------------------------------------------------

    with facet_left:

        st.subheader("Product Line")

        st.caption(
            "Counts respect the current price filter, "
            "but ignore the selected Product Line."
        )

        st.dataframe(
            pd.DataFrame(product_line_facets),
            use_container_width=True,
            hide_index=True
        )


    # -------------------------------------------------
    # Price
    # -------------------------------------------------

    with facet_right:

        st.subheader("Price")

        st.caption(
            "Counts respect the selected Product Line, "
            "but ignore the current price range."
        )

        st.dataframe(
            pd.DataFrame(price_facets),
            use_container_width=True,
            hide_index=True
        )


# =====================================================
# Debug tab
# =====================================================

with tab_debug:

    st.subheader("Query")

    st.json(query)


    st.subheader("Post Filter")

    st.json(post_filter)


    st.subheader("Aggregations")

    st.json(aggregations)