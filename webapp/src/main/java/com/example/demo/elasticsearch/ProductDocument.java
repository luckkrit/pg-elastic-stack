package com.example.demo.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
public class ProductDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String productcode;

    @Field(type = FieldType.Text)
    private String productname;

    @Field(type = FieldType.Keyword)
    private String productline;

    @Field(type = FieldType.Text)
    private String productdescription;

    @Field(type = FieldType.Keyword)
    private String productscale;

    @Field(type = FieldType.Keyword)
    private String productvendor;

    @Field(type = FieldType.Float)
    private Double buyprice;

    @Field(type = FieldType.Float)
    private Double msrp;

    @Field(type = FieldType.Integer)
    private Integer quantityinstock;

    public ProductDocument() {
    }

    public ProductDocument(
            String productcode,
            String productname,
            String productline,
            String productdescription,
            String productscale,
            String productvendor,
            Double buyprice,
            Double msrp,
            Integer quantityinstock) {

        this.productcode = productcode;
        this.productname = productname;
        this.productline = productline;
        this.productdescription = productdescription;
        this.productscale = productscale;
        this.productvendor = productvendor;
        this.buyprice = buyprice;
        this.msrp = msrp;
        this.quantityinstock = quantityinstock;
    }

    public String getProductcode() {
        return productcode;
    }

    public void setProductcode(String productcode) {
        this.productcode = productcode;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getProductline() {
        return productline;
    }

    public void setProductline(String productline) {
        this.productline = productline;
    }

    public String getProductdescription() {
        return productdescription;
    }

    public void setProductdescription(String productdescription) {
        this.productdescription = productdescription;
    }

    public String getProductscale() {
        return productscale;
    }

    public void setProductscale(String productscale) {
        this.productscale = productscale;
    }

    public String getProductvendor() {
        return productvendor;
    }

    public void setProductvendor(String productvendor) {
        this.productvendor = productvendor;
    }

    public Double getBuyprice() {
        return buyprice;
    }

    public void setBuyprice(Double buyprice) {
        this.buyprice = buyprice;
    }

    public Double getMsrp() {
        return msrp;
    }

    public void setMsrp(Double msrp) {
        this.msrp = msrp;
    }

    public Integer getQuantityinstock() {
        return quantityinstock;
    }

    public void setQuantityinstock(Integer quantityinstock) {
        this.quantityinstock = quantityinstock;
    }
}