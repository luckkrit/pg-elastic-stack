import { getProducts } from '#/server/product'
import { createFileRoute, Link } from '@tanstack/react-router'
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
export const Route = createFileRoute('/products/')({
  loader: () => getProducts(),
  component: RouteComponent,
})

function RouteComponent() {
  const products = Route.useLoaderData()
  return (
    // <ul>
    //   {products.map((p) => (
    //     <li key={p.productcode}>
    //       <Link to="/products/$productcode" params={{ productcode: p.productcode }}>
    //         {p.productname}
    //       </Link>
    //     </li>
    //   ))}
    // </ul>
    <Table>
      <TableCaption>A list of your recent invoices.</TableCaption>
      <TableHeader>
        <TableRow>
          <TableHead>productcode</TableHead>
          <TableHead>productname</TableHead>
          <TableHead>productline</TableHead>
          <TableHead>productscale</TableHead>
          <TableHead>productvendor</TableHead>
          <TableHead>productdescription</TableHead>
          <TableHead>quantityinstock</TableHead>
          <TableHead>buyprice</TableHead>
          <TableHead>msrp</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {products.map((product) => (
          <TableRow key={product.productcode}>
            <TableCell className="font-medium">{product.productcode}</TableCell>
            <TableCell>{product.productname}</TableCell>
            <TableCell>{product.productline}</TableCell>
            <TableCell>{product.productscale}</TableCell>
            <TableCell>{product.productvendor}</TableCell>
            <TableCell>{product.productdescription}</TableCell>
            <TableCell>{product.quantityinstock}</TableCell>
            <TableCell className="text-right">${product.buyprice}</TableCell>
            <TableCell className="text-right">${product.msrp}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
