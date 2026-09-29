<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="c" uri="jakarta.tags.core" %>
            <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
                <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

                    <t:layout title="Products" activePage="products">
                        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/marks.css">
                        <h2 class="text-xl font-semibold my-2">Search Products</h2>

                        <%-- Search Form for use in product list --%>
                            <c:if test="${empty addProduct}">
                                <form action="${pageContext.request.contextPath}/product" method="get"
                                    class="flex items-center gap-4">
                                    <input type="text" name="q" value="${fn:escapeXml(q)}" placeholder="Search products"
                                        class="input input-bordered">

                                    <label class="label cursor-pointer gap-2">
                                        <input type="checkbox" name="es" class="checkbox" ${es ? 'checked' : '' }>
                                        <span class="label-text">Use Elasticsearch</span>
                                    </label>

                                    <button type="submit" class="btn btn-primary">Search</button>
                                </form>

                                <p>Total: ${fn:length(products)}</p>
                                <table id="results" class="table" data-query="${fn:escapeXml(q)}">
                                    <thead class="sticky top-0 bg-base-100">
                                        <tr class="border-b">
                                            <th>Product Code</th>
                                            <th>Product Line</th>
                                            <th>Product Name</th>
                                            <th>Product Description</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        <c:forEach var="product" items="${products}">
                                            <tr class="border-b">
                                                <td>${product.productCode}</td>
                                                <td class="no-highlight">${product.productLine}</td>
                                                <td>${product.productName}</td>
                                                <td>${product.productDescription}</td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:if>
                            <c:if test="${not es}">
                                <script src="${pageContext.request.contextPath}/js/marks.js"></script>
                            </c:if>
                    </t:layout>