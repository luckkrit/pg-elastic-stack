export default defineEventHandler(async () => {
    const db = useDb()

    const result = await db.query(`
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
  `)

    return result.rows
})