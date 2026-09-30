export default defineEventHandler(async (event) => {
    const config = useRuntimeConfig()

    const query = getQuery(event)
    const q = String(query.q ?? '')

    return await $fetch(
        `${config.elasticsearchUrl}/products/_search`,
        {
            method: 'POST',
            body: {
                size: 20,

                query: q
                    ? {
                        multi_match: {
                            query: q,
                            fields: [
                                'productname',
                                'productline',
                                'productdescription'
                            ]
                        }
                    }
                    : {
                        match_all: {}
                    }
            }
        }
    )
})