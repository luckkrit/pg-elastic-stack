<script setup lang="ts">
interface Product {
    productcode: string
    productname: string
    productline: string
    productdescription: string
    buyprice: number
    msrp: number
    quantityinstock: number
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
    }
}

const keyword = ref('')

const { data, refresh, pending, error } =
    await useFetch<SearchResponse>('/api/products', {
        query: {
            q: keyword
        }
    })

function search() {
    refresh()
}
</script>

<template>
    <main>
        <h1>Product Search</h1>

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
        </div>
    </main>
</template>