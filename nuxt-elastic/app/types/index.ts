export interface Product {
    productcode: string
    productname: string
    productline: string
    productdescription: string
    buyprice: number
    msrp: number
    quantityinstock: number
}
export interface ProductRow {

    id: string
    score: number
    productcode: string
    productname: string
    productline: string
    productdescription: string
    buyprice: number
    msrp: number
    quantityinstock: number
}
export interface Bucket {
    key: string
    from: number
    to: number
    doc_count: number
}
export interface SearchHit {
    _id: string
    _score: number
    _source: Product,
    highlight?: Highlight
}
export interface Highlight {
    productname?: string[]
    productdescription?: string[]
}
export interface SearchResponse {
    hits: {
        total: {
            value: number
            relation: string
        }
        hits: SearchHit[],
    },
    aggregations: {
        by_productline: {
            buckets: Bucket[]
        },
        by_price: {
            buckets: Bucket[]
        }
    }
}
