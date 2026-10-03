export default defineEventHandler(async (event) => {
    const id = event.context.params?.id
    const db = useDb()

    if (id === undefined) {
        throw createError({
            statusCode: 400,
            statusMessage: 'Missing product id'
        })
    }
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
    WHERE productcode = $1
  `, [id])

    return result.rows
})