export default defineEventHandler(async (event) => {
    const config = useRuntimeConfig()

    const query = getQuery(event)
    const q = String(query.q ?? '')

    return await $fetch(
        `${config.elasticsearchUrl}/products/_search`,
        {
            method: 'POST',
            body: {
                size: 40,
                query: {
                    match_all: {}
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
        }
    )
})
