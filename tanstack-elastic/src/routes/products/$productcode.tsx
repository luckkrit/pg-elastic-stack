import { getProduct } from '#/server/product'
import { createFileRoute, notFound } from '@tanstack/react-router'

export const Route = createFileRoute('/products/$productcode')({
loader: async ({ params }) => {
    const product = await getProduct({ data: params.productcode })
    if (!product) throw notFound()
    return product
  },
  component: RouteComponent,
})

function RouteComponent() {
  const product = Route.useLoaderData() // type-safe อัตโนมัติ
  return (<div>{product.productname}</div>)

}