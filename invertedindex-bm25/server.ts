import { engine } from './index'
Bun.serve({
    port: 3000,
    fetch(req) {
        const url = new URL(req.url);

        if (url.pathname === "/search") {
            const q = url.searchParams.get("q") ?? "";
            return Response.json(engine.search(q));
        }

        return new Response("Not found", { status: 404 });
    },
});