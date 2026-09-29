package org.eclipse.classic.web.model;

public class Product {

    private String productCode;
    private String productName;
    private String productDescription;

    private String productLine;
    private Double buyPrice;
    private Double msrp;

    public Product() {
    }

    public Product(String productCode, String productName, String productDescription, String productLine,
            Double buyPrice, Double msrp) {
        this.productCode = productCode;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productLine = productLine;
        this.buyPrice = buyPrice;
        this.msrp = msrp;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Double getMsrp() {
        return msrp;
    }

    public void setMsrp(Double msrp) {
        this.msrp = msrp;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
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
}