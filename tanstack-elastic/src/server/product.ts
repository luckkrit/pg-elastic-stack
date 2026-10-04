// src/server/products.ts
import { createServerFn } from "@tanstack/react-start";
import { sql } from "./db";
import { z } from "zod";

export const getProducts = createServerFn({ method: "GET" }).handler(
  async () => {
    return sql`
    SELECT
      productcode,
      productname,
      productline,
      productscale,
      productvendor,
      productdescription,
      quantityinstock,
      buyprice,
      msrp
    FROM classicmodels.products
    ORDER BY productname
    LIMIT 50
    `;
  },
);

export const getProduct = createServerFn({ method: "GET" })
  .validator(z.string())
  .handler(async ({ data }) => {
    const rows = await sql`
      select * from classicmodels.products where productcode = ${data}
    `;
    return rows[0] ?? null;
  });
