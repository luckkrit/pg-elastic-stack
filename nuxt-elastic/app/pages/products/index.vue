<script setup lang="ts">
import type { Product } from '~/types';

const toast = useToast()
const { data, refresh, pending, error } =
    await useFetch<Product[]>('/api/products', {
        query: {
        },
        watch: false
    })

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
        <h1 class="font-bold text-xl mb-2">Products</h1>
        <UTable :data="data" class="flex-1" :ui="{ td: 'whitespace-normal' }">
            <template #productcode-cell="{ row }">
                <ULink :exact="false" :to="`/products/${row.original.productcode}`"
                    class="text-primary hover:underline">{{ row.original.productcode }}
                </ULink>
            </template>
            <template #productdescription-cell="{ row }">
                <div class="whitespace-normal" v-html="row.original.productdescription"></div>
            </template>
            <template #productname-cell="{ row }">
                <div class="whitespace-normal" v-html="row.original.productname"></div>
            </template>
        </UTable>
    </main>
</template>