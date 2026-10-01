<script setup lang="ts">
import type { CheckboxGroupItem } from '@nuxt/ui'
interface Row {

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
interface Product {
    productcode: string
    productname: string
    productline: string
    productdescription: string
    buyprice: number
    msrp: number
    quantityinstock: number
}
interface Bucket {
    key: string
    from: number
    to: number
    doc_count: number
}
interface SearchHit {
    _id: string
    _score: number
    _source: Product,
    highlight?: Highlight
}
interface Highlight {
    productname?: string[]
    productdescription?: string[]
}
interface SearchResponse {
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

const selectedProductLines = ref<string>()
const selectedPriceRanges = ref<string>()
const keyword = ref('')
const toast = useToast()
const { data, refresh, pending, error } =
    await useFetch<SearchResponse>('/api/products', {
        query: {
            q: keyword,
            productline: selectedProductLines,
            rangePrice: selectedPriceRanges
        },
        watch: false
    })

function search() {
    refresh()
}
watch(
    [selectedProductLines, selectedPriceRanges],
    () => {
        refresh()
    },
    { deep: true }
)
watch(pending, (isPending) => {
    if (isPending) {
        toast.add({
            id: 'loading',
            title: 'Loading...',
            icon: 'i-lucide-loader-circle',
            duration: 0 // stays open until manually closed/updated
        })
    } else {
        toast.remove('loading')
    }
})

watch(error, (err) => {
    if (err) {
        toast.remove('loading')
        toast.add({
            title: 'Something went wrong',
            description: err.message,
            icon: 'i-lucide-alert-circle',
            color: 'error'
        })
    }
})
const rows = computed<Row[]>(() => {
    if (data.value?.hits && Array.isArray(data.value?.hits.hits)) {
        const raws = data.value?.hits.hits.map((hit: SearchHit) => {
            const productdescription = hit.highlight?.productdescription?.join(',').replaceAll('<mark>', '<UBadge color="neutral" variant="outline">') || hit._source.productdescription
            const productname = hit.highlight?.productname?.join(',').replaceAll('<mark>', '<UBadge color="neutral" variant="outline">') || hit._source.productname
            return ({
                id: hit._id,
                score: hit._score,
                ...hit._source,
                productdescription,
                productname
            })
        }) ?? []
        return raws
    }
    return []
}
)
const productLines = computed<CheckboxGroupItem[]>(() => {
    if (data.value?.aggregations?.by_productline && Array.isArray(data.value?.aggregations?.by_productline?.buckets)) {
        const lines = data.value?.aggregations?.by_productline.buckets.map((bucket) => ({ label: `${bucket.key} (${bucket.doc_count})`, description: '', value: bucket.key }))
        const none: CheckboxGroupItem = { label: 'None', description: '', value: undefined }
        return [none, ...lines]
    }
    return []
})

const priceRanges = computed<CheckboxGroupItem[]>(() => {
    if (data.value?.aggregations?.by_price && Array.isArray(data.value?.aggregations?.by_price?.buckets)) {
        const rangePrices = data.value?.aggregations?.by_price.buckets.map((bucket) => ({ label: `${bucket.key} (${bucket.doc_count})`, description: '', value: `${bucket.from ?? 0}:${bucket.to ?? 0}` }))
        const none: CheckboxGroupItem = { label: 'None', description: '', value: undefined }
        return [none, ...rangePrices]
    }
    return []
})

</script>

<template>
    <main class="py-2">
        <h1 class="font-bold text-xl mb-2">Product Search</h1>
        <div class="flex gap-2">
            <div class="w-60 p-2 ">
                <h3 class="font-bold">
                    Product Line
                </h3>
                <div class="p-2 w-full">
                    <URadioGroup v-model="selectedProductLines" :items="productLines" />
                </div>
                <h3 class="font-bold">
                    Price Range
                </h3>
                <div class="p-2 w-full">
                    <div class="p-2 w-full">
                        <URadioGroup v-model="selectedPriceRanges" :items="priceRanges" />
                    </div>
                </div>
            </div>
            <div class="flex-1 p-2 ">
                <div>
                    <UFieldGroup>
                        <UInput v-model="keyword" placeholder="Search..." @keyup.enter="search" />
                        <UButton icon="i-lucide-search" @click="search" />
                    </UFieldGroup>
                    <UTable :data="rows" class="flex-1" :ui="{ td: 'whitespace-normal' }">
                        <template #productdescription-cell="{ row }">
                            <div class="whitespace-normal" v-html="row.original.productdescription"></div>
                        </template>
                        <template #productname-cell="{ row }">
                            <div class="whitespace-normal" v-html="row.original.productname"></div>
                        </template>
                    </UTable>
                </div>
            </div>
        </div>
    </main>
</template>