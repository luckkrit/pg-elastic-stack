<script setup lang="ts">
import type { Product } from '~/types';

const route = useRoute()
const id = route.params.id

const toast = useToast()
const { data, pending, error } = await useFetch<Product[]>(`/api/products/${id}`)

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
</script>
<template>

    <main class="py-2">
        <h1 class="font-bold text-xl mb-2">Product Details</h1>
        <div v-if="data && data.length > 0">
            <div v-for="product in data" :key="product.productcode">
                <p><strong>Product Code:</strong>
                    <ULink :to="`/products/${product.productcode}`">&nbsp;&nbsp;{{ product.productcode }}</ULink>
                </p>
                <p><strong>Product Name:</strong> {{ product.productname }}</p>
                <p><strong>Product Line:</strong> {{ product.productline }}</p>
                <p><strong>Description:</strong> {{ product.productdescription }}</p>
                <p><strong>Buy Price:</strong> {{ product.buyprice }}</p>
                <p><strong>MSRP:</strong> {{ product.msrp }}</p>
                <p><strong>Quantity in Stock:</strong> {{ product.quantityinstock }}</p>
            </div>
        </div>
        <div v-else>
            <p>No data</p>
        </div>
    </main>
</template>