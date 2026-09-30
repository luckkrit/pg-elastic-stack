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
    _source: Product
}

interface SearchResponse {
    hits: {
        total: {
            value: number
            relation: string
        }
        hits: SearchHit[]
    },
    aggregations: {
        by_productline: {
            buckets: Bucket[]
        }
    }
}

const keyword = ref('')
const toast = useToast()
const { data, refresh, pending, error } =
    await useFetch<SearchResponse>('/api/products', {
        query: {
            q: keyword
        }
    })

function search() {
    refresh()
}
const selectedProductLines = ref<string[]>([])
const priceRanges = ref<string[]>([])
const selectedPriceRanges = ref<string[]>([])
watch(pending, (isPending) => {
    if (isPending) {
        toast.add({
            id: 'loading',
            title: 'Loading...',
            icon: 'i-lucide-loader-circle',
            duration: 0 // stays open until manually closed/updated
        })
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
        return data.value?.hits?.hits.map((hit: SearchHit) => ({
            id: hit._id,
            score: hit._score,
            ...hit._source
        })) ?? []
    }
    return []
}
)
const productLines = computed<CheckboxGroupItem[]>(() => {
    console.log(data.value)
    if (data.value?.aggregations?.by_productline && Array.isArray(data.value?.aggregations?.by_productline?.buckets)) {
        return data.value?.aggregations?.by_productline.buckets.map((bucket) => ({ label: `${bucket.key} (${bucket.doc_count})`, description: '', value: bucket.key }))
    }
    return []
})
watch(selectedProductLines, (data) => {
    console.log(data)
})
</script>

<template>
    <main class="py-2">
        <h1 class="font-bold text-xl mb-2">Product Search</h1>
        <div class="flex gap-2">
            <div class="w-60 p-2 bg-lime-50">
                <h3 class="font-bold">
                    Product Line
                </h3>
                <div class="p-2 w-full">
                    <UCheckboxGroup v-model="selectedProductLines" :items="productLines" />
                </div>
                <h3 class="font-bold">
                    Price Range
                </h3>
                <div class="p-2 w-full">
                    <div class="p-2 w-full">
                        <UCheckboxGroup v-model="selectedPriceRanges" :items="priceRanges" />
                    </div>
                </div>
            </div>
            <div class="flex-1 p-2 bg-slate-200">
                <div>
                    <UInput v-model="keyword" placeholder="Search product..." @keyup.enter="search" />
                    <UTable :data="rows" class="flex-1" />
                </div>
            </div>
        </div>
        <!-- 
        <input v-model="keyword" placeholder="Search product..." @keyup.enter="search">

        <button @click="search">
            Search
        </button>

        <p v-if="pending">
            Loading...
        </p>

        <p v-if="error">
            {{ error }}
        </p>

        <div v-for="hit in data?.hits.hits ?? []" :key="hit._id">
            <h3>{{ hit._source.productname }}</h3>

            <p>{{ hit._source.productline }}</p>

            <p>Price: {{ hit._source.buyprice }}</p>

            <p>Score: {{ hit._score }}</p>
        </div> -->
    </main>
</template>