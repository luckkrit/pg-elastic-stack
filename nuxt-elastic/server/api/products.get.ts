export default defineEventHandler(async (event) => {
    const config = useRuntimeConfig()
    const query = getQuery(event)

    const q = String(query.q ?? '').trim()
    const productline = query.productline ? String(query.productline).trim() : ''
    const rangePrice = String(query.rangePrice ?? '').trim()

    // "50:100" -> 50..100, "200:" -> 200 and up, ":50" -> up to 50, "" -> no price filter
    const parseBound = (raw?: string): number | null => {
        if (raw === undefined || raw.trim() === '') return null
        const n = Number(raw)
        return Number.isFinite(n) ? n : null
    }
    const [minRaw, maxRaw] = rangePrice.split(':')
    const minPrice = parseBound(minRaw)
    const maxPrice = parseBound(maxRaw)

    // post_filter: the user's selections, so they affect hits only
    const filters: any[] = []
    if (productline) {
        filters.push({ term: { productline } })
    }
    if (minPrice !== null || maxPrice !== null) {
        const rangeCondition: Record<string, number> = {}
        if (minPrice !== null) rangeCondition.gte = minPrice
        if (maxPrice !== null) rangeCondition.lt = maxPrice   // lt, to match the half-open buckets
        filters.push({ range: { buyprice: rangeCondition } })
    }

    const searchBody: Record<string, any> = {
        size: 40,
        // q goes in query, so it affects hits AND facet counts
        query: q === ''
            ? { match_all: {} }
            : { multi_match: { query: q, fields: ['productname', 'productdescription'] } },
        aggs: {
            by_productline: {
                terms: { field: 'productline' }
            },
            by_price: {
                range: {
                    field: 'buyprice',
                    ranges: [
                        { key: 'Under $50', to: 50 },
                        { key: '$50 - $99.99', from: 50, to: 100 },
                        { key: '$100 - $199.99', from: 100, to: 200 },
                        { key: '$200 and above', from: 200 }
                    ]
                }
            }
        }
    }

    if (filters.length > 0) {
        searchBody.post_filter = { bool: { filter: filters } }
    }

    if (import.meta.dev) {
        console.log('ES query:', JSON.stringify(searchBody))
    }

    try {
        return await $fetch(`${config.elasticsearchUrl}/products/_search`, {
            method: 'POST',
            body: searchBody
        })
    } catch (err: any) {
        throw createError({
            statusCode: 502,
            statusMessage: 'Elasticsearch request failed',
            data: err?.data ?? err?.message
        })
    }
})