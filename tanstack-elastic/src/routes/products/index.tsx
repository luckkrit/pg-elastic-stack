import { getProducts } from '#/server/product'
import { createFileRoute, Link } from '@tanstack/react-router'

export const Route = createFileRoute('/products/')({
  loader: () => getProducts(),    
  component: RouteComponent,
})

function RouteComponent() {
  const products = Route.useLoaderData() 
return (
    <ul>
      {products.map((p) => (
        <li key={p.productcode}>
          <Link to="/products/$productcode" params={{ productcode: p.productcode }}>
            {p.productname}
          </Link>
        </li>
      ))}
    </ul>
  )
}
