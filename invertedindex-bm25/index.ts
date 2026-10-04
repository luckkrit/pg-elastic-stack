interface Posting {
    docId: number;
    tf: number;
}

export interface BM25Response {
    docId: number;
    score: number;
    tf: number;
    idf: number;
    k1: number;
    b: number;
    dl: number;
    avgdl: number;
    query: string;
    source: string;
}

class InvertedIndex {
    index: Map<string, Posting[]> = new Map();

    add(term: string, posting: Posting) {
        if (!this.index.has(term)) {
            this.index.set(term, []);
        }
        this.index.get(term)!.push(posting);
    }

    get(term: string): Posting[] {
        return this.index.get(term) || [];
    }
}

class DocumentStore {
    docs: Map<number, string[]> = new Map();
    raws: Map<number, string> = new Map();
    totalTokens = 0;

    add(docId: number, text: string, tokens: string[]) {
        if (this.docs.has(docId)) {
            this.totalTokens -= this.docs.get(docId)!.length;
        }
        this.totalTokens += tokens.length;
        this.docs.set(docId, tokens);
        this.raws.set(docId, text);
    }

    getLength(docId: number): number {
        return this.docs.get(docId)?.length || 0;
    }

    getSource(docId: number): string {
        return this.raws.get(docId) || "";
    }

    size(): number {
        return this.docs.size;
    }

    getAvgdl(): number {
        return this.size() === 0 ? 0 : this.totalTokens / this.size();
    }
}

class BM25SearchEngine {
    docs = new DocumentStore();
    index = new InvertedIndex();

    constructor(public k1 = 1.2, public b = 0.75) { }

    tokenize(text: string): string[] {
        return text.toLowerCase().match(/\b\w+\b/g) || [];
    }

    calculateIdf(term: string): number {
        const postings = this.index.get(term);
        const df = postings.length;
        if (df === 0) return 0;
        return Math.log((this.docs.size() - df + 0.5) / (df + 0.5) + 1);
    }

    calculateBM25(tf: number, idf: number, docLen: number, avgdl: number): number {
        if (tf <= 0 || idf <= 0) return 0;
        const safeAvgdl = avgdl === 0 ? 1 : avgdl;

        const numerator = tf * (this.k1 + 1);
        const denominator = tf + this.k1 * (1 - this.b + this.b * (docLen / safeAvgdl));
        return idf * (numerator / denominator);
    }

    addDocument(docId: number, text: string) {
        const tokens = this.tokenize(text);
        this.docs.add(docId, text, tokens);

        const tfCounts = new Map<string, number>();
        for (const token of tokens) {
            tfCounts.set(token, (tfCounts.get(token) || 0) + 1);
        }

        for (const [term, tf] of tfCounts.entries()) {
            this.index.add(term, { docId, tf });
        }
    }

    search(query: string): BM25Response[] {
        const queryTokens = this.tokenize(query);
        const results = new Map<number, Omit<BM25Response, "docId">>();
        const avgdl = this.docs.getAvgdl();

        for (const term of queryTokens) {
            const postings = this.index.get(term);
            if (postings.length === 0) continue;

            const idf = this.calculateIdf(term);

            for (const { docId, tf } of postings) {
                const dl = this.docs.getLength(docId);
                const score = this.calculateBM25(tf, idf, dl, avgdl);
                const prev = results.get(docId);

                results.set(docId, {
                    score: (prev?.score || 0) + score,
                    tf: (prev?.tf || 0) + tf,
                    idf: (prev?.idf || 0) + idf,
                    k1: this.k1,
                    b: this.b,
                    dl,
                    avgdl,
                    query,
                    source: this.docs.getSource(docId),
                });
            }
        }

        return Array.from(results.entries())
            .sort((a, b) => b[1].score - a[1].score)
            .map(([docId, data]) => ({ docId, ...data }));
    }
}
const corpus = {
    1: "elasticsearch is a search engine",
    2: "search search search is fun",
    3: "kibana shows data in charts",
    4: "learn python for data science",
    5: "search data is so fun"
};

const engine = new BM25SearchEngine();
for (const [docId, text] of Object.entries(corpus)) {
    engine.addDocument(Number(docId), text);
}

console.log(engine.search("search data"));


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