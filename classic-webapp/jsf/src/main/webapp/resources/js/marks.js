const results = document.querySelector("#results");
const query = results.dataset.query ?? "";
console.log("query = ", query)
    const words = query.trim().split(/\s+/).filter(w => w.length > 0);
    if (words.length > 0) {
        new Mark(document.querySelector('#results')).mark(words, { exclude: [".no-highlight"] });
    }