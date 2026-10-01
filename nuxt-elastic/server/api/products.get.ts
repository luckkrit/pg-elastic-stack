export default defineEventHandler(async (event) => {
    const config = useRuntimeConfig()

    const query = getQuery(event)
    const q = String(query.q ?? '').trim()
    const productline = query.productline ? String(query.productline) : null
    const rangePrice = String(query.rangePrice ?? '').trim()
    // const minPrice = query.min_price ? Number(query.min_price) : null
    // const maxPrice = query.max_price ? Number(query.max_price) : null
    let minPrice = null
    let maxPrice = null
    const prices = rangePrice.trim().split(':')
    if (prices.length > 1) {
        minPrice = Number(prices[0])
        maxPrice = Number(prices[1])
    }
    console.log(rangePrice, minPrice, maxPrice, `q = ${q}`, `productline = ${productline}`)
    const filters: any[] = []
    if (productline) {
        filters.push({
            term: { "productline": productline }
        })
    }
    if (minPrice !== null || maxPrice !== null) {
        const rangeCondition: Record<string, number> = {}
        if (minPrice !== null) rangeCondition.gte = minPrice
        if (maxPrice !== null) rangeCondition.lte = maxPrice

        filters.push({
            range: { buyprice: rangeCondition }
        })
    }
    const searchBody: Record<string, any> = {
        size: 40,
        query:
            q == '' ? {
                match_all: {}
            } : {
                multi_match: {
                    query: q,
                    fields: [
                        'productname',
                        'productdescription'
                    ]
                }
            },
        aggs: {
            by_productline: {
                terms: {
                    field: "productline"
                }
            },
            by_price: {
                range: {
                    field: "buyprice",
                    ranges: [
                        {
                            "key": "Under $50",
                            "to": 50
                        },
                        {
                            "key": "$50 - $99.99",
                            "from": 50,
                            "to": 100
                        },
                        {
                            "key": "$100 - $199.99",
                            "from": 100,
                            "to": 200
                        },
                        {
                            "key": "$200 and above",
                            "from": 200
                        }
                    ]
                }
            }
        }
    }
    if (filters.length > 0) {
        searchBody.post_filter = {
            bool: {
                filter: filters
            }
        }
    }
    console.log(JSON.stringify(searchBody))
    return await $fetch(
        `${config.elasticsearchUrl}/products/_search`,
        {
            method: 'POST',
            body: searchBody
        }
    )
})
