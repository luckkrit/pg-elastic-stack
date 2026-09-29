<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Products - JSP</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 30px;
        }

        table {
            border-collapse: collapse;
            width: 100%;
        }

        th, td {
            border: 1px solid #ccc;
            padding: 8px 12px; text-align: left;
        }

        th {
            background-color: #f3f3f3;
        }

        tr:nth-child(even) {
            background-color: #fafafa;
        }
    </style>
</head>

<body>
<h1>Products - JSP</h1>
<table>
    <thead>
    <tr>
        <th>Product Code</th>
        <th>Product Name</th>
        <th>Product Line</th>
        <th>Buy Price</th>
        <th>MSRP</th>
    </tr>
    </thead>

    <tbody>
    <c:forEach var="product" items="${products}">
        <tr>
            <td>${product.productCode}</td>
            <td>${product.productName}</td>
            <td>${product.productLine}</td>
            <td>${product.buyPrice}</td>
            <td>${product.msrp}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</body>
</html>