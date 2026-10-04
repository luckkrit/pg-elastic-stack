interface Posting {
    docId: number;
    tf: number;
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
        return this.index.get(term) || []
    }
}

class Documents {
    index: InvertedIndex;
    docs: Map<number, string[]>;
    avgdl: number
    constructor() {
        this.avgdl = 0;
        this.index = new InvertedIndex()
        this.docs = new Map()
    }
    tokenize(text: string) {
        return text.toLowerCase().match(/\b\w+\b/g) || [];
    }
    addDocument(docId: number, text: string) {
        const tokens = this.tokenize(text)
        this.docs.set(docId, tokens)
        const tfCounts = new Map();
        for (const token of tokens) {
            tfCounts.set(token, (tfCounts.get(token) || 0) + 1);
        }
        for (const [term, tf] of tfCounts.entries()) {
            this.index.add(term, { docId, tf })
        }
        let totalLength = 0;
        for (const tokens of this.docs.values()) {
            totalLength += tokens.length;
        }
        if (this.docs.size <= 0) {
            return;
        }
        this.avgdl = totalLength / this.docs.size;
    }
    docLength(docId: number) {
        const tokens = this.docs.get(docId) || []
        return tokens.length
    }
    size() {
        return this.docs.size;
    }
}

class FulltextSearch {
    docs: Documents;
    k1: number;
    b: number;
    constructor(k1 = 1.2, b = 0.75) {
        this.docs = new Documents();
        this.k1 = k1;
        this.b = b;
    }
    calculateIdf(term: string) {
        const postings = this.docs.index.get(term) || [];
        const df = postings.length;
        if (df === 0) return 0;

        // สูตร IDF เดียวกับมาตรฐาน Lucene / Python
        return Math.log((this.docs.size() - df + 0.5) / (df + 0.5) + 1);
    }
    search(query: string) {
        const queryTokens = this.docs.tokenize(query);
        const scores = new Map();

        for (const term of queryTokens) {
            const postings = this.docs.index.get(term);
            if (!postings) continue;

            const idf = this.calculateIdf(term);

            for (const { docId, tf } of postings) {
                const docLen = this.docs.docLength(docId);

                // สูตร BM25 Weight
                const numerator = tf * (this.k1 + 1);
                const denominator = tf + this.k1 * (1 - this.b + this.b * (docLen / this.docs.avgdl));
                const score = idf * (numerator / denominator);

                scores.set(docId, (scores.get(docId) || 0) + score);
            }
        }

        // เรียงคะแนนจากมากไปน้อย
        return Array.from(scores.entries())
            .sort((a, b) => b[1] - a[1])
            .map(([docId, score]) => ({ docId, score }));
    }
}

const corpus = {
    1: "elasticsearch is a search engine",
    2: "search search search is fun",
    3: "kibana shows data in charts",
    4: "learn python for data science"
};

const engine = new FulltextSearch();
for (const [id, text] of Object.entries(corpus)) {
    engine.docs.addDocument(Number(id), text);
}

console.log(engine.search("search"))
