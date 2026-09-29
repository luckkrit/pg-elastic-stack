<%@ tag description="Main layout" pageEncoding="UTF-8" %>
    <%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
        <%@ attribute name="title" required="false" %>
            <%@ attribute name="activePage" required="false" %>
                <!DOCTYPE html>
                <html lang="th">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
                    <link href="https://cdn.jsdelivr.net/npm/daisyui@5" rel="stylesheet" type="text/css" />
                    <script src="https://cdn.jsdelivr.net/mark.js/8.6.0/mark.min.js"></script>
                    <title>${empty title ? 'DEMO' : title}</title>
                </head>

                <body>
                    <t:navbar activePage="${activePage}" />
                    <main class="container pt-16 pl-2 pr-2 pb-2">
                        <jsp:doBody />
                    </main>
                </body>

                </html>