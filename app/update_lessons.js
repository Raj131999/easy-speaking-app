const fs = require('fs');
const path = 'app/src/main/assets/useful_lessons.json';
const lessons = JSON.parse(fs.readFileSync(path, 'utf8'));

function createExamples(list) {
    return list.map(item => `• ${item.category ? item.category + ': ' : ''}${item.text}`).join('\n');
}

// Map of custom exact multi-examples for key lessons to guarantee perfection
const customExamples = {
    101: [ // Lesson 1: HAVE TO VS MUST
        { category: "Boss / Workplace Instruction (have to)", text: "My manager told me I have to finish this presentation before tomorrow's meeting." },
        { category: "Company Policy (have to)", text: "All employees have to submit their weekly timesheets by Friday evening." },
        { category: "Law / Official Regulation (have to)", text: "Drivers have to stop when the traffic light turns red." },
        { category: "Self-Imposed Duty (must)", text: "I must call my mother tonight because I miss her." },
        { category: "Conscious / Internal Duty (must)", text: "I feel I must buy fresh vegetables and cook a healthy meal tonight." }
    ],
    102: [ // Lesson 2: USE OF 'HAVE TO' AND 'MUST'
        { category: "Affirmative Obligation (have to)", text: "You have to wear a seat belt when driving on the highway." },
        { category: "Negative Prohibition (must not / mustn't)", text: "You must not leave your homework incomplete." },
        { category: "Lack of Necessity (don't have to)", text: "You don't have to wear a tie to work on Casual Friday." },
        { category: "Past Obligation (had to)", text: "Yesterday, I had to work late to complete the quarterly audit." },
        { category: "Future Obligation (will have to)", text: "Next week, we will have to submit our project proposals." }
    ],
    103: [ // Lesson 3: HAVE TO, OUGHT TO & SUPPOSED TO
        { category: "No Choice (have to / must)", text: "I have to submit the financial reports tomorrow morning." },
        { category: "Expected Rule with Choice (supposed to)", text: "We are supposed to submit the reports by tomorrow, but we can request an extension." },
        { category: "Advice / Suggestion (ought to)", text: "You ought to get some sleep before your early flight." }
    ],
    104: [ // Lesson 4: HAVE TO, SUPPOSED TO & OUGHT TO
        { category: "Obligation (have to)", text: "I have to pay my property taxes by the end of the month." },
        { category: "Expected Behavior (supposed to)", text: "You are supposed to keep your phone on silent during the presentation." },
        { category: "Moral Duty / Suggestion (ought to)", text: "We ought to help our elderly neighbors carry their heavy groceries." },
        { category: "Negative Expected Rule (not supposed to)", text: "You are not supposed to talk loudly inside a hospital." }
    ],
    105: [ // Lesson 5: USING 'MUST' CORRECTLY
        { category: "Personal Obligation", text: "I must finish reading this textbook before my exam on Monday." },
        { category: "Logical Certainty", text: "He has been working in the garden all afternoon, so he must be tired." },
        { category: "Strong Recommendation", text: "You must visit that new Italian restaurant downtown—the food is incredible." },
        { category: "Question Form (Do you have to?)", text: "Do you have to leave so early, or can you stay for coffee?" }
    ],
    106: [ // Lesson 6: MUSTN'T vs NEEDN'T
        { category: "Prohibition / Restriction (mustn't)", text: "You mustn't tell anyone our secret company plans." },
        { category: "Lack of Necessity / Optional (needn't)", text: "You needn't carry physical documents with you since everything is online." }
    ],
    107: [ // Lesson 7: USING 'NEED' CORRECTLY
        { category: "Main Verb (need to)", text: "I need to buy some groceries for dinner." },
        { category: "Modal Verb (needn't)", text: "You needn't worry about the reservations; I already booked the table." },
        { category: "Noun Form (in need of)", text: "The shelter is in urgent need of clean blankets." }
    ],
    108: [ // Lesson 8: ALL USES OF SHOULD
        { category: "Giving Advice", text: "You should see a doctor if your fever doesn't go down." },
        { category: "Expecting Probability", text: "The train should arrive in about ten minutes." },
        { category: "Negative Advice (shouldn't)", text: "You shouldn't drink coffee right before going to bed." },
        { category: "Past Regret (should have)", text: "I should have studied harder for the final exam." }
    ],
    109: [ // Lesson 9: USES OF 'SHOULD'
        { category: "Recommendation", text: "You should try the roasted salmon at this café." },
        { category: "Logical Expectation", text: "They left an hour ago, so they should be here soon." },
        { category: "Criticism / Regret (should have)", text: "You should have warned me about the heavy traffic." }
    ],
    110: [ // Lesson 10: USING 'SHOULD' IN DIFFERENT WAYS
        { category: "Future Expectation (should)", text: "He should reach the office any minute now." },
        { category: "Negative Suggestion (shouldn't)", text: "You shouldn't stay up late watching television." },
        { category: "Past Regret (should have)", text: "I shouldn't have shouted at you during the meeting yesterday." }
    ],
    111: [ // Lesson 11: MODAL VERB 'COULD'
        { category: "Past Ability (could)", text: "When I was younger, I could run ten miles without stopping." },
        { category: "Present Possibility (could)", text: "It could rain later this afternoon, so take an umbrella." },
        { category: "Polite Request (Could you...)", text: "Could you please pass me the water pitcher?" },
        { category: "Past Possibility / Missed Opportunity (could have)", text: "I could have won the race, but I tripped near the finish line." }
    ],
    121: [ // Lesson 21: MAY HAVE VS MIGHT HAVE VS MUST HAVE
        { category: "Around 50% Certainty (may have)", text: "She may have gone to the library after class." },
        { category: "Slight Possibility (might have)", text: "I might have left my keys in the office, but I am not sure." },
        { category: "High Certainty / Evidence (must have)", text: "He must have left his phone at home because he isn't answering." }
    ],
    151: [ // Lesson 51: Dynamic vs Stative Uses
        { category: "Opinion (Stative)", text: "I think this is a fantastic opportunity for all of us." },
        { category: "Mental Activity (Dynamic)", text: "I am thinking about moving to a new apartment next month." },
        { category: "Possession (Stative)", text: "She has a large house near the lake." },
        { category: "Experience / Action (Dynamic)", text: "We are having lunch at a terrace restaurant." }
    ],
    201: [ // Lesson 101: Accept vs Except
        { category: "Verb: Receive / Agree to (accept)", text: "I gladly accept your invitation to the anniversary dinner." },
        { category: "Preposition: Excluding (except)", text: "Everyone arrived on time for the meeting except for Mark." }
    ],
    202: [ // Lesson 102: Loose vs Lose
        { category: "Adjective: Not tight (loose)", text: "This sweater is very loose on me after losing weight." },
        { category: "Verb: Misplace or fail to win (lose)", text: "Be careful not to lose your passport at the airport." }
    ],
    203: [ // Lesson 103: Everyday vs Every day
        { category: "Adjective: Ordinary / Routine (everyday)", text: "These are my everyday shoes for walking in the neighborhood." },
        { category: "Adverbial Phrase: Each day (every day)", text: "I practice speaking English every day for thirty minutes." }
    ],
    204: [ // Lesson 104: Then vs Than
        { category: "Time / Next step (then)", text: "We went to dinner, and then we watched a movie." },
        { category: "Comparison (than)", text: "She is taller than her elder brother." }
    ],
    205: [ // Lesson 105: Their vs There vs They're
        { category: "Possessive pronoun (their)", text: "The students forgot their textbooks in the classroom." },
        { category: "Location / Existent (there)", text: "There is a quiet park right around the corner." },
        { category: "Contraction of 'They are' (they're)", text: "They're planning to visit us during the winter holidays." }
    ],
    225: [ // Lesson 125: Fewer vs Less
        { category: "Countable Nouns (fewer)", text: "There were fewer attendees at the workshop today than yesterday." },
        { category: "Uncountable Nouns (less)", text: "Please add less sugar and more lemon to my iced tea." }
    ],
    230: [ // Lesson 130: Subject vs Object Pronouns
        { category: "Subject Pronoun (I, he, she, we, they)", text: "She and I completed the project ahead of schedule." },
        { category: "Object Pronoun (me, him, her, us, them)", text: "The manager invited him and me to present the findings." }
    ],
    251: [ // Lesson 151: Quantifiers: 'Much', 'Many', & 'A Lot Of'
        { category: "Countable Nouns (many)", text: "There aren't many open seats remaining in the auditorium." },
        { category: "Uncountable Nouns (much)", text: "How much time do we have before our flight boards?" },
        { category: "Both Noun Types (a lot of)", text: "She has a lot of friends and a lot of confidence." }
    ],
    301: [ // Lesson 201: Expanding Academic & Business Vocabulary
        { category: "Formal Alternative: 'big' -> 'substantial'", text: "The department demonstrated a substantial improvement in quarterly revenue." },
        { category: "Formal Alternative: 'change' -> 'modify'", text: "We need to modify our marketing campaign to target a broader audience." },
        { category: "Formal Alternative: 'help' -> 'assist'", text: "Our team will assist you with the onboarding process." },
        { category: "Formal Alternative: 'show' -> 'demonstrate'", text: "The research data clearly demonstrates the effectiveness of the treatment." }
    ],
    331: [ // Lesson 231: Plural Noun Rules & Irregular Plurals
        { category: "Irregular Plurals (vowel change)", text: "The men and women worked together to fix the garden fence." },
        { category: "Identical Singular/Plural (sheep, deer, fish)", text: "A flock of sheep was grazing peacefully in the green meadow." },
        { category: "Always-Plural Nouns (pants, scissors, glasses)", text: "Where did I place my reading glasses and scissors?" }
    ],
    334: [ // Lesson 234: Using 'Suggest' & 'Recommend' Correctly
        { category: "Structure: suggest + -ing", text: "I suggest taking a short fifteen-minute walk to clear your mind." },
        { category: "Structure: suggest that + subject + base verb", text: "I suggest that he review the document before submitting it." },
        { category: "Structure: recommend that + subject + base verb", text: "The doctor recommended that she rest for three full days." }
    ]
};

// Process all lessons
lessons.forEach(l => {
    const id = l.id;
    if (customExamples[id]) {
        l.exampleText = createExamples(customExamples[id]);
    } else {
        l.exampleText = generateGenericExpanded(l);
    }
});

function generateGenericExpanded(l) {
    const title = l.title;
    const exp = l.explanation;
    const curEx = l.exampleText.replace(/^Example:\s*/i, '');
    const speech = l.speechPrompt.replace(/^Example:\s*/i, '');

    // Extract categories or terms from title or explanation
    let items = [];

    // Check for "vs", "or", "/", "&" in title
    const titleClean = title.replace(/^Lesson \d+\s*—\s*/i, '');
    if (titleClean.includes(' vs ') || titleClean.includes(' VS ') || titleClean.includes(' vs. ')) {
        const parts = titleClean.split(/ vs\.? | VS\.? /i).map(s => s.trim().replace(/Rules|Masterclass|Correctly|Usage/gi, '').trim());
        if (parts.length >= 2) {
            items.push({ category: `${parts[0]} (Concept 1)`, text: curEx });
            items.push({ category: `${parts[1]} (Concept 2)`, text: speech });
            return createExamples(items);
        }
    }

    // Split explanation into sentences
    const sentences = exp.split(/(?<=[.!?])\s+/).filter(s => s.trim().length > 15);
    if (sentences.length >= 2) {
        sentences.forEach((s, i) => {
            let cat = `Rule / Category ${i + 1}`;
            // Look for quoted words or parenthetical examples in the sentence
            const quotes = s.match(/'([^']+)'/g) || s.match(/"([^"]+)"/g);
            if (quotes && quotes.length > 0) {
                cat = quotes.map(q => q.replace(/['"]/g, '')).join(' / ');
            } else {
                const parens = s.match(/\(([^)]+)\)/);
                if (parens) {
                    cat = parens[1].replace(/e\.g\.?,?\s*/gi, '').substring(0, 30);
                }
            }
            items.push({
                category: cat,
                text: i === 0 ? curEx : (i === 1 ? speech : `Example sentence illustrating ${cat}: "${curEx}"`)
            });
        });
        if (items.length >= 2) return createExamples(items);
    }

    // Default 2 examples
    return createExamples([
        { category: "Primary Rule / Example", text: curEx },
        { category: "Key Usage Context", text: speech }
    ]);
}

fs.writeFileSync(path, JSON.stringify(lessons, null, 2), 'utf8');
console.log(`Updated all ${lessons.length} lessons in useful_lessons.json successfully!`);
