package com.example.demo.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
public class ProductDocument {

    @Id
    @Field(name = "productcode", type = FieldType.Keyword)
    private String productCode;

    @Field(name = "productname", type = FieldType.Text)
    private String productName;

    @Field(name = "productline", type = FieldType.Keyword)
    private String productLine;

    @Field(name = "productdescription", type = FieldType.Text)
    private String productDescription;

    @Field(name = "productscale", type = FieldType.Keyword)
    private String productScale;

    @Field(name = "productvendor", type = FieldType.Keyword)
    private String productVendor;

    @Field(name = "buyprice", type = FieldType.Float)
    private Double buyPrice;

    @Field(name = "msrp", type = FieldType.Float)
    private Double msrp;

    @Field(name = "quantityinstock", type = FieldType.Integer)
    private Integer quantityInStock;

    public ProductDocument() {
    }

    public ProductDocument(
            String productCode,
            String productName,
            String productLine,
            String productDescription,
            String productScale,
            String productVendor,
            Double buyPrice,
            Double msrp,
            Integer quantityInStock) {

        this.productCode = productCode;
        this.productName = productName;
        this.productLine = productLine;
        this.productDescription = productDescription;
        this.productScale = productScale;
        this.productVendor = productVendor;
        this.buyPrice = buyPrice;
        this.msrp = msrp;
        this.quantityInStock = quantityInStock;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductLine() {
        return productLine;
    }

    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductScale() {
        return productScale;
    }

    public void setProductScale(String productScale) {
        this.productScale = productScale;
    }

    public String getProductVendor() {
        return productVendor;
    }

    public void setProductVendor(String productVendor) {
        this.productVendor = productVendor;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getMsrp() {
        return msrp;
    }

    public void setMsrp(Double msrp) {
        this.msrp = msrp;
    }

    public Integer getQuantityInStock() {
        return quantityInStock;
    }

    public void setQuantityInStock(Integer quantityInStock) {
        this.quantityInStock = quantityInStock;
    }
}