import json
import os

def generate_lessons():
    json_path = "app/src/main/assets/useful_lessons.json"
    with open(json_path, "r", encoding="utf-8") as f:
        existing_lessons = json.load(f)

    # Keep lessons 1 to 31 (indices 0 to 30) exactly as they are
    final_lessons = list(existing_lessons[:31])

    # Detailed specifications for Lessons 32 to 234
    lesson_specs = [
        # Auxiliary Verbs (32-36)
        {
            "id": 132, "category": "Auxiliary Verbs — Do / Does / Did / Have / Has / Be", "level": "Beginner",
            "title": "Lesson 32 — Auxiliary Verbs: 'Do' and 'Does' in Questions & Negatives",
            "explanation": "Use 'do' with I, you, we, they and 'does' with he, she, it to form present simple questions and negative statements. Remember that after 'does' or 'doesn't', the main verb returns to its base form (e.g., 'Does she like...?' not 'likes').",
            "exampleText": "Does she speak English fluently at work?",
            "speechPrompt": "Do you prefer tea or coffee in the morning?",
            "optionsString": "Do,Does,Is,Has", "correctOption": "Does"
        },
        {
            "id": 133, "category": "Auxiliary Verbs — Do / Does / Did / Have / Has / Be", "level": "Beginner",
            "title": "Lesson 33 — Auxiliary Verb 'Did' in Past Simple Tense",
            "explanation": "Use 'did' for all subjects (I, you, he, she, we, they) when forming questions or negatives in the past simple. Once 'did' or 'didn't' is used, the main verb stays in its base form.",
            "exampleText": "Did you complete the project report yesterday?",
            "speechPrompt": "I didn't hear the phone ring during the meeting.",
            "optionsString": "Did,Do,Done,Were", "correctOption": "Did"
        },
        {
            "id": 134, "category": "Auxiliary Verbs — Do / Does / Did / Have / Has / Be", "level": "Beginner",
            "title": "Lesson 34 — Auxiliary 'Have' & 'Has' in Present Perfect Sentences",
            "explanation": "Use 'have' (I, you, we, they) or 'has' (he, she, it) combined with the past participle (V3) to form the present perfect tense, describing actions with present relevance.",
            "exampleText": "She has lived in London for five years.",
            "speechPrompt": "We have finished all our assignments for this week.",
            "optionsString": "have,has,had,having", "correctOption": "has"
        },
        {
            "id": 135, "category": "Auxiliary Verbs — Do / Does / Did / Have / Has / Be", "level": "Beginner",
            "title": "Lesson 35 — Auxiliary 'Be' (Am/Is/Are/Was/Were) in Continuous Tenses",
            "explanation": "The auxiliary verb 'be' combines with present participles (-ing) to create continuous tenses, showing ongoing actions in the present or past.",
            "exampleText": "They were discussing the new budget when I entered.",
            "speechPrompt": "I am currently preparing for my English fluency test.",
            "optionsString": "is,are,were,be", "correctOption": "were"
        },
        {
            "id": 136, "category": "Auxiliary Verbs — Do / Does / Did / Have / Has / Be", "level": "Intermediate",
            "title": "Lesson 36 — Emphatic 'Do', 'Does', and 'Did' for Strong Assertions",
            "explanation": "You can place 'do', 'does', or 'did' before a main verb in affirmative sentences to add strong emphasis or contrast a previous doubt (e.g., 'I do care about your progress').",
            "exampleText": "I do appreciate all the support you have given me.",
            "speechPrompt": "She does know how to solve this difficult problem.",
            "optionsString": "do,am,have,really", "correctOption": "do"
        },

        # Perfect Tenses (37-44)
        {
            "id": 137, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Intermediate",
            "title": "Lesson 37 — Present Perfect Simple: Life Experiences & Recent Actions",
            "explanation": "Present Perfect Simple connects past events with the present moment. Use it for life experiences ('I have visited Japan'), accomplishments, or recent events without specifying an exact past time.",
            "exampleText": "I have already seen that movie twice.",
            "speechPrompt": "Have you ever traveled abroad on your own?",
            "optionsString": "saw,have seen,did see,had seen", "correctOption": "have seen"
        },
        {
            "id": 138, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Intermediate",
            "title": "Lesson 38 — Present Perfect Continuous: Ongoing Unfinished Actions",
            "explanation": "Formed with 'have/has been + verb-ing', this tense emphasizes the duration or ongoing nature of an action that started in the past and continues into the present.",
            "exampleText": "She has been studying English for three hours.",
            "speechPrompt": "It has been raining continuously since morning.",
            "optionsString": "has been studying,is studying,studied,has studied", "correctOption": "has been studying"
        },
        {
            "id": 139, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Intermediate",
            "title": "Lesson 39 — Present Perfect Simple vs Present Perfect Continuous",
            "explanation": "Use Present Perfect Simple when focusing on completion, result, or quantity ('I have written three letters'). Use Present Perfect Continuous when focusing on duration, process, or activity ('I have been writing all morning').",
            "exampleText": "He has been painting the room all day, and he has painted two walls so far.",
            "speechPrompt": "I have been calling him, but he hasn't answered yet.",
            "optionsString": "painted,has been painting,was painting,had painted", "correctOption": "has been painting"
        },
        {
            "id": 140, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Intermediate",
            "title": "Lesson 40 — Past Perfect Simple ('Had Done') for Earlier Past Actions",
            "explanation": "Past Perfect Simple ('had + V3') indicates an action that happened before another past event or a specific point in the past.",
            "exampleText": "When I arrived at the station, the train had already left.",
            "speechPrompt": "She had finished her work before the manager asked for it.",
            "optionsString": "left,has left,had left,was leaving", "correctOption": "had left"
        },
        {
            "id": 141, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Intermediate",
            "title": "Lesson 41 — Past Perfect Continuous ('Had Been Doing')",
            "explanation": "Use 'had been + verb-ing' to describe an ongoing action that was taking place up until another moment in the past, emphasizing duration.",
            "exampleText": "They had been waiting for over an hour before the bus finally arrived.",
            "speechPrompt": "I had been working there for five years when the company closed.",
            "optionsString": "had been waiting,were waiting,have been waiting,waited", "correctOption": "had been waiting"
        },
        {
            "id": 142, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Advanced",
            "title": "Lesson 42 — Future Perfect Simple ('Will Have Done')",
            "explanation": "Use 'will have + past participle' to state that an action will be completed before a specified time in the future.",
            "exampleText": "By next month, I will have completed my degree.",
            "speechPrompt": "They will have finished the building by December.",
            "optionsString": "will complete,will have completed,completed,have completed", "correctOption": "will have completed"
        },
        {
            "id": 143, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Advanced",
            "title": "Lesson 43 — Future Perfect Continuous ('Will Have Been Doing')",
            "explanation": "Use 'will have been + verb-ing' to project into the future and express the duration of an ongoing action up to a given future point.",
            "exampleText": "By 5 PM, she will have been working for eight hours straight.",
            "speechPrompt": "Next year, we will have been living here for a decade.",
            "optionsString": "will have been working,will work,is working,will be working", "correctOption": "will have been working"
        },
        {
            "id": 144, "category": "Perfect Tenses (Present, Past & Future Perfect / Perfect Continuous)", "level": "Advanced",
            "title": "Lesson 44 — Master Perfect Tenses in Conversational Fluency",
            "explanation": "Mastering perfect tenses allows you to tell stories with accurate time sequencing and express subtle relationships between past, present, and future events.",
            "exampleText": "I had been hoping to meet her, but she had already left.",
            "speechPrompt": "How long have you been practicing English today?",
            "optionsString": "have you been,had you been,were you,are you", "correctOption": "have you been"
        },

        # Past & Present Tenses (45-51)
        {
            "id": 145, "category": "Past & Present Tenses (Simple and Continuous)", "level": "Beginner",
            "title": "Lesson 45 — Simple Present Tense: Habits, Routines & Universal Truths",
            "explanation": "Use simple present for regular habits, daily routines, facts, and permanent situations. Add '-s' or '-es' for third-person singular subjects (he, she, it).",
            "exampleText": "The sun rises in the east every morning.",
            "speechPrompt": "He plays tennis every Saturday morning with his friends.",
            "optionsString": "rise,rises,rising,rose", "correctOption": "rises"
        },
        {
            "id": 146, "category": "Past & Present Tenses (Simple and Continuous)", "level": "Beginner",
            "title": "Lesson 46 — Present Continuous Tense: Actions Happening Right Now",
            "explanation": "Use 'am/is/are + verb-ing' to describe actions taking place at the exact moment of speaking or temporary situations occurring around now.",
            "exampleText": "Please be quiet; the baby is sleeping.",
            "speechPrompt": "I am reading an interesting book on psychology.",
            "optionsString": "sleeps,is sleeping,was sleeping,slept", "correctOption": "is sleeping"
        },
        {
            "id": 147, "category": "Past & Present Tenses (Simple and Continuous)", "level": "Beginner",
            "title": "Lesson 47 — Simple Past Tense: Completed Past Actions & Dates",
            "explanation": "Use simple past to describe finished actions that occurred at a specific time in the past. Regular verbs take '-ed', while irregular verbs change form (e.g., go -> went).",
            "exampleText": "We visited our grandparents last weekend.",
            "speechPrompt": "I bought a new laptop yesterday afternoon.",
            "optionsString": "visit,visited,visiting,have visited", "correctOption": "visited"
        },
        {
            "id": 148, "category": "Past & Present Tenses (Simple and Continuous)", "level": "Beginner",
            "title": "Lesson 48 — Past Continuous Tense: Ongoing Background Past Actions",
            "explanation": "Use 'was/were + verb-ing' to set a scene or describe an action that was in progress at a specific time in the past.",
            "exampleText": "At 8 PM yesterday, I was watching a documentary.",
            "speechPrompt": "They were playing football when it started to rain.",
            "optionsString": "watched,was watching,am watching,watch", "correctOption": "was watching"
        },
        {
            "id": 149, "category": "Past & Present Tenses (Simple and Continuous)", "level": "Intermediate",
            "title": "Lesson 49 — Simple Past vs Past Continuous: Interrupted Actions",
            "explanation": "Combine Past Continuous (background long action) and Simple Past (short interrupting action) using 'when' or 'while' (e.g., 'While I was cooking, the doorbell rang').",
            "exampleText": "I was sleeping when the alarm rang.",
            "speechPrompt": "While she was driving to work, she saw a magnificent rainbow.",
            "optionsString": "slept,was sleeping,sleep,had slept", "correctOption": "was sleeping"
        },
        {
            "id": 150, "category": "Past & Present Tenses (Simple and Continuous)", "level": "Intermediate",
            "title": "Lesson 50 — Stative Verbs: Why Certain Verbs Don't Take '-ing'",
            "explanation": "Stative verbs describe states of mind, emotions, senses, and possession (e.g., love, know, want, belong, understand). They are normally used in simple tenses, not continuous forms.",
            "exampleText": "I understand the grammar rule clearly now.",
            "speechPrompt": "She knows the answer to that tricky question.",
            "optionsString": "am understanding,understand,understood,was understanding", "correctOption": "understand"
        },
        {
            "id": 151, "category": "Past & Present Tenses (Simple and Continuous)", "level": "Intermediate",
            "title": "Lesson 51 — Dynamic vs Stative Uses of Verbs (Think, Have, See)",
            "explanation": "Some verbs can be both stative and dynamic depending on meaning. For example, 'I think you are right' (opinion - stative) vs 'I am thinking about the offer' (mental activity - dynamic).",
            "exampleText": "I am thinking about moving to a new city.",
            "speechPrompt": "I think this is a fantastic opportunity for all of us.",
            "optionsString": "think,am thinking,thought,thinks", "correctOption": "am thinking"
        },

        # Future Tense (52-53)
        {
            "id": 152, "category": "Future Tense", "level": "Intermediate",
            "title": "Lesson 52 — Expressing Future: 'Will' vs 'Going To' vs Present Continuous",
            "explanation": "Use 'will' for spontaneous decisions or predictions based on opinion. Use 'be going to' for prior plans or evidence-based predictions. Use Present Continuous for fixed arrangements.",
            "exampleText": "I am meeting the doctor tomorrow at 10 AM.",
            "speechPrompt": "Look at those dark clouds; it is going to rain soon.",
            "optionsString": "will meet,am meeting,meet,met", "correctOption": "am meeting"
        },
        {
            "id": 153, "category": "Future Tense", "level": "Intermediate",
            "title": "Lesson 53 — Future Time Clauses: Present Simple After 'When', 'If', 'As Soon As'",
            "explanation": "In future time clauses introduced by conjunctions like when, as soon as, before, after, or if, use the Present Simple tense to refer to future time.",
            "exampleText": "I will call you as soon as I arrive at the hotel.",
            "speechPrompt": "When he comes home, we will start dinner immediately.",
            "optionsString": "arrive,will arrive,arrived,arriving", "correctOption": "arrive"
        },

        # Questions & Question Formation (54-57)
        {
            "id": 154, "category": "Questions & Question Formation (incl. Tag Questions & Wh-Questions)", "level": "Beginner",
            "title": "Lesson 54 — Wh- Question Formation: Word Order Rules",
            "explanation": "Standard question structure: Wh- Word + Auxiliary Verb + Subject + Main Verb (e.g., 'Where do you live?'). Ensure the auxiliary verb matches the tense and subject.",
            "exampleText": "Where did you buy that beautiful jacket?",
            "speechPrompt": "What time does the morning train depart?",
            "optionsString": "you bought,did you buy,bought you,do you bought", "correctOption": "did you buy"
        },
        {
            "id": 155, "category": "Questions & Question Formation (incl. Tag Questions & Wh-Questions)", "level": "Intermediate",
            "title": "Lesson 55 — Subject Questions vs Object Questions",
            "explanation": "If the question word asks about the subject, do not use auxiliary verbs 'do/does/did' (e.g., 'Who broke the vase?'). If it asks about the object, use auxiliary verbs (e.g., 'Who did you call?').",
            "exampleText": "Who called you late last night?",
            "speechPrompt": "Who gave you this wonderful advice?",
            "optionsString": "Who called,Who did call,Who calling,Who was called", "correctOption": "Who called"
        },
        {
            "id": 156, "category": "Questions & Question Formation (incl. Tag Questions & Wh-Questions)", "level": "Intermediate",
            "title": "Lesson 56 — Question Tags: Positive/Negative Matching Rules",
            "explanation": "Question tags confirm information. A positive statement takes a negative tag ('You live here, don't you?'), and a negative statement takes a positive tag ('You aren't tired, are you?').",
            "exampleText": "She is coming to the party tonight, isn't she?",
            "speechPrompt": "You haven't seen my car keys, have you?",
            "optionsString": "is she,isn't she,doesn't she,was she", "correctOption": "isn't she"
        },
        {
            "id": 157, "category": "Questions & Question Formation (incl. Tag Questions & Wh-Questions)", "level": "Intermediate",
            "title": "Lesson 57 — Indirect Questions for Polite Conversation",
            "explanation": "Indirect questions sound polite (e.g., 'Could you tell me where the bank is?'). Notice that in indirect questions, the word order changes back to normal statement order (subject before verb).",
            "exampleText": "Do you know where the nearest post office is?",
            "speechPrompt": "I was wondering if you could help me with this task.",
            "optionsString": "where is the bank,where the bank is,the bank where is,is where the bank", "correctOption": "where the bank is"
        },

        # Conditional Sentences (58-60)
        {
            "id": 158, "category": "Conditional Sentences & Subjunctive Mood", "level": "Intermediate",
            "title": "Lesson 58 — Zero & First Conditionals: Facts & Real Possibilities",
            "explanation": "Zero conditional (If + Present Simple, Present Simple) states general scientific facts ('If water boils, it turns to steam'). First conditional (If + Present Simple, Will + Verb) expresses real future possibilities.",
            "exampleText": "If it rains tomorrow, we will stay at home.",
            "speechPrompt": "If you study consistently, you will improve your English skills.",
            "optionsString": "will rain,rains,rained,is raining", "correctOption": "rains"
        },
        {
            "id": 159, "category": "Conditional Sentences & Subjunctive Mood", "level": "Intermediate",
            "title": "Lesson 59 — Second Conditional: Hypothetical Present & Advice",
            "explanation": "Use Second Conditional (If + Past Simple, Would + Base Verb) for hypothetical or imaginary present situations. Use 'If I were you...' to give friendly advice.",
            "exampleText": "If I had a million dollars, I would buy a house on the coast.",
            "speechPrompt": "If I were you, I would take that job offer immediately.",
            "optionsString": "have,had,would have,will have", "correctOption": "had"
        },
        {
            "id": 160, "category": "Conditional Sentences & Subjunctive Mood", "level": "Advanced",
            "title": "Lesson 60 — Third Conditional: Unreal Past & Regrets",
            "explanation": "Use Third Conditional (If + Past Perfect, Would Have + V3) to talk about imaginary past events that did not happen and express regret or relief.",
            "exampleText": "If I had set my alarm, I would not have missed the bus.",
            "speechPrompt": "If we had left earlier, we would have avoided the traffic jams.",
            "optionsString": "had set,set,would set,have set", "correctOption": "had set"
        }
    ]

    # Generate remaining lessons up to 234 dynamically with curated high quality entries
    # Let's verify the count currently in lesson_specs: len is 29 (from 32 to 60)
    # Now build 61 to 234

    start_id = 161
    current_order = 61

    # Categories to map from existing asset items:
    # Get remaining items from existing_lessons[31:] to extract exact category names
    asset_items = existing_lessons[31:]

    # Map out sub-topics per index
    subtopic_db = [
        # Conjunctions (61-67)
        ("Coordinating Conjunctions: FANBOYS Rules", "Coordinating conjunctions (For, And, Nor, But, Or, Yet, So) join equal clauses or words. Always place a comma before them when connecting independent clauses.", "I wanted to go for a walk, but it began to rain.", "He was exhausted, yet he continued running.", "and,but,or,so", "but"),
        ("Subordinating Conjunctions: Reason & Cause", "Use subordinating conjunctions like because, since, as, and due to the fact that to introduce clauses explaining reasons.", "Since it was holiday, the offices were closed.", "We stayed inside because the weather was stormy.", "Because,Since,So,Although", "Since"),
        ("Correlative Conjunctions: Pairs in Harmony", "Correlative conjunctions work in pairs (both...and, either...or, neither...nor, not only...but also). Ensure parallel grammatical structures after each part.", "Not only did he win the prize, but he also set a new record.", "You can either choose the red coat or the blue jacket.", "either...or,neither...nor,both...and,not only...but also", "not only...but also"),
        ("Conjunctions of Contrast: Although vs Despite", "Although, even though, and though take a full clause (subject + verb). Despite and in spite of take a noun phrase or gerund (-ing).", "Although it was cold, we went swimming.", "Despite the heavy rain, they arrived on time.", "Although,Despite,However,In spite", "Although"),
        ("Conjunctions of Purpose & Result: So That & In Order To", "Use 'so that + clause' or 'in order to + verb' to express purpose and intention clearly.", "She saved money so that she could buy a car.", "He woke up early in order to catch the first train.", "so that,in order to,because,unless", "so that"),
        ("Sentence Connectors: However, Furthermore, Therefore", "Transition words connect ideas between separate sentences. Place a semicolon or period before them and a comma immediately after.", "The exam was difficult; however, most students passed.", "She is very talented; furthermore, she works extremely hard.", "however,therefore,moreover,in contrast", "however"),
        ("Mastering Conjunctions for Cohesive Speech", "Connecting thoughts smoothly with appropriate conjunctions eliminates choppy sentences and boosts spoken fluency.", "I enjoy reading books, whereas my brother prefers watching movies.", "Although I was tired, I finished my work before midnight.", "whereas,although,because,despite", "whereas"),

        # Prepositions (68-86)
        ("Prepositions of Time: In, On, At Detailed Rules", "Use 'at' for specific times/moments (at 5 PM, at noon), 'on' for days and dates (on Monday, on May 1st), and 'in' for longer periods (in July, in 2024, in the morning).", "The train leaves at 9:30 AM on Tuesday.", "My birthday is in September.", "at,on,in,by", "at"),
        ("Prepositions of Place: In, On, At Spatial Concepts", "Use 'at' for specific points or locations (at the door), 'on' for surfaces (on the table), and 'in' for enclosed spaces or areas (in the room, in London).", "She is sitting at her desk in the main office.", "There is a beautiful painting on the wall.", "at,on,in,by", "at"),
        ("Prepositions of Movement: Into, Onto, Through, Across", "Prepositions of movement describe direction. 'Into' means entering an enclosed space; 'onto' means moving to a surface; 'through' means passing inside a 3D space.", "He walked into the building and went up the stairs.", "The boat sailed across the calm river.", "into,onto,through,across", "into"),
        ("Dependent Prepositions with Popular Adjectives", "Many adjectives are paired with specific prepositions (e.g., interested in, good at, famous for, afraid of, allergic to). Memorize these fixed pairs.", "She is exceptionally good at solving complex math puzzles.", "He is deeply interested in learning ancient history.", "at,in,of,for", "at"),
        ("Dependent Prepositions with Common Verbs", "Verbs often take specific prepositions (e.g., depend on, belong to, listen to, suffer from, apology for).", "Success depends on your dedication and effort.", "Who does this briefcase belong to?", "on,to,from,for", "on"),
        ("Prepositional Phrases with 'In': Fixed Idiomatic Uses", "Phrases like 'in detail', 'in advance', 'in fact', 'in public', and 'in brief' are set expressions in spoken and written English.", "Please let us know your decision in advance.", "In fact, she had already solved the problem.", "in,at,on,by", "in"),
        ("Prepositional Phrases with 'At': Common Expressions", "Master fixed phrases like 'at last', 'at least', 'at present', 'at first glance', and 'at risk'.", "At last, the rainy weather cleared up.", "You should spend at least thirty minutes reading daily.", "at,in,on,by", "at"),
        ("Prepositional Phrases with 'On': On Time vs On Purpose", "Phrases like 'on time' (punctual), 'on purpose' (intentionally), 'on duty', and 'on average' add precision to your speech.", "He showed up right on time for the meeting.", "I am certain he didn't break the vase on purpose.", "on,in,at,by", "on"),
        ("Prepositional Phrases with 'By': Transportation & Accident", "Use 'by' for modes of transit (by bus, by train, by air) and accidental causes (by chance, by mistake). Note: 'on foot' is an exception.", "I grabbed his umbrella by mistake.", "They traveled across Europe by train.", "by,on,in,with", "by"),
        ("Prepositions 'Under' & 'Over' in Idiomatic Phrases", "Phrases like 'under pressure', 'under construction', 'over time', and 'over the weekend' describe continuous conditions.", "She works exceptionally well under pressure.", "Road repairs will be completed over the weekend.", "under,over,above,below", "under"),
        ("Differences: 'In the end' vs 'At the end'", "'At the end' refers to the final point of something tangible (at the end of the road/movie). 'In the end' means 'finally' or 'eventually' after a process.", "In the end, everything worked out perfectly.", "There is a summary at the end of each chapter.", "In the end,At the end,To the end,By the end", "In the end"),
        ("Differences: 'On time' vs 'In time'", "'On time' means punctual according to a schedule. 'In time' means early enough before a deadline or event.", "The flight landed right on time.", "We arrived in time to see the opening ceremony.", "on time,in time,at time,by time", "on time"),
        ("Prepositions Following Nouns (Reason for, Solution to)", "Nouns take specific prepositions: reason for, solution to, advantage of, relationship with, impact on.", "What was the main reason for the sudden delay?", "Scientists are working on a solution to climate change.", "for,to,of,on", "for"),
        ("Prepositions of Cause & Reason: Due To vs Because Of", "'Because of' is followed by a noun phrase. 'Due to' functions similarly but often follows a link verb like 'is' or 'was'.", "The match was canceled because of heavy rainfall.", "His late arrival was due to severe traffic.", "because of,due to,owing to,thanks to", "because of"),
        ("Prepositions with Means of Transport & Vehicles", "Use 'in' for private vehicles (in a car, in a taxi). Use 'on' for public or large transport (on a bus, on a plane, on a train).", "I saw him sitting on the subway train this morning.", "We rode in my friend's car to the beach.", "on,in,by,at", "on"),
        ("Eliminating Common Preposition Errors in Speaking", "Avoid adding unnecessary prepositions after verbs like discuss ('discuss the topic', NOT 'discuss about'), enter ('enter the room'), or answer ('answer the question').", "We need to discuss the project timeline today.", "She entered the office silently.", "discuss,discuss about,discuss on,discuss over", "discuss"),
        ("Advanced Prepositions: Besides, Except For, Apart From", "'Besides' means 'in addition to'. 'Except for' or 'apart from' means excluding or leaving out.", "Besides English, she speaks Spanish and French fluently.", "Everyone attended the seminar except for John.", "Besides,Except for,Apart from,Beside", "Besides"),
        ("Prepositions in Official Phrases: According to & With Regard to", "Use 'according to' when citing sources and 'with regard to' when introducing a business topic.", "According to the news report, temperatures will drop tomorrow.", "I am writing with regard to your recent job application.", "According to,With regard to,In spite of,Due to", "According to"),
        ("Preposition Placement in Questions & Preposition Stranding", "In conversational questions, prepositions naturally fall at the end of the clause ('What are you looking at?' / 'Who did you talk to?').", "What kind of music are you listening to?", "Who were you speaking with on the phone?", "to,at,with,about", "to"),

        # Confusing Word Pairs & Usage (87-129)
        ("Borrow vs Lend vs Keep", "'Borrow' means to take temporarily ('Can I borrow your pen?'). 'Lend' means to give temporarily ('I will lend you my book'). 'Keep' means to hold permanently or long-term.", "Could you lend me your umbrella until tomorrow?", "I borrowed a coat from my brother for the winter trip.", "lend,borrow,keep,take", "lend"),
        ("Rob vs Steal", "'Rob' focuses on the victim or place robbed ('They robbed the bank'). 'Steal' focuses on the item taken ('He stole a wallet').", "Someone stole my bicycle from the driveway.", "The thieves robbed three local jewelry stores.", "stole,robbed,taken,snatched", "stole"),
        ("Study vs Learn vs Teach", "'Study' is the process of reading/practicing. 'Learn' is acquiring knowledge or skill. 'Teach' is imparting knowledge to others.", "She wants to learn how to play the piano.", "He studies for two hours every evening.", "learn,study,teach,master", "learn"),
        ("Rise vs Raise vs Arise", "'Rise' is intransitive (no direct object: 'Prices rise'). 'Raise' is transitive (takes an object: 'Raise your hand'). 'Arise' means to come into notice or happen.", "Please raise your hand if you have a question.", "The sun will rise at six in the morning.", "raise,rise,arise,rose", "raise"),
        ("Lie vs Lay vs Lain", "'Lie' means to recline or rest (intransitive: 'Lie down'). 'Lay' means to place something down (transitive: 'Lay the book on the table'). Past tense of lie is lay; past of lay is laid.", "Please lay the blanket gently on the grass.", "I need to lie down for a short nap.", "lay,lie,laid,lain", "lay"),
        ("Say vs Tell vs Speak vs Talk", "Use 'tell' with a personal object ('Tell me the story'). Use 'say' without a personal object ('Say hello'). Use 'speak/talk' for languages or general dialogue.", "She told me that she was leaving early.", "He said that he would call us back.", "told,said,spoke,talked", "told"),
        ("Hear vs Listen", "'Hear' is passive perception of sound. 'Listen' is active, focused attention (requires 'to': 'Listen to the music').", "Did you hear that strange noise outside?", "Please listen carefully to the speaker.", "hear,listen,listening,heard", "hear"),
        ("See vs Look vs Watch", "'See' is automatic visual perception. 'Look' is directing your eyes at something ('Look at this'). 'Watch' is paying attention to something moving over time.", "We sat on the couch to watch a movie.", "Look at those colorful flowers in the garden.", "watch,see,look,observe", "watch"),
        ("Make vs Do", "Use 'do' for actions, obligations, jobs, and repetitive tasks ('do homework, do research'). Use 'make' for creating, producing, or causing results ('make dinner, make mistakes').", "I need to make an important phone call now.", "She does her best in every project.", "make,do,made,done", "make"),
        ("Bring vs Take vs Fetch", "'Bring' is movement toward the speaker ('Bring it here'). 'Take' is movement away from the speaker ('Take this to him'). 'Fetch' is going, getting, and returning.", "Please bring me a glass of water from the kitchen.", "Don't forget to take your umbrella when you leave.", "bring,take,fetch,carry", "bring"),
        ("Come vs Go", "'Come' indicates movement toward the speaker or listener's location. 'Go' indicates movement away from the current location.", "Are you coming to my party tonight?", "We are planning to go to Spain next summer.", "coming,going,come,go", "coming"),
        ("Remember vs Remind", "'Remember' is keeping or recalling memory yourself. 'Remind' is causing someone else to remember ('Remind me to call').", "Please remind me to send that email before 5 PM.", "I distinctly remember locking the front door.", "remind,remember,recalling,mind", "remind"),
        ("Advise vs Advice", "'Advise' is the verb (pronounced /ədˈvaɪz/). 'Advice' is the uncountable noun (pronounced /ədˈvaɪs/).", "My doctor advised me to get more exercise.", "She gave me valuable advice about my career.", "advised,advice,advising,advices", "advised"),
        ("Effect vs Affect", "'Affect' is usually a verb meaning to influence. 'Effect' is usually a noun meaning a result or consequence.", "The rainy weather will affect our outdoor plans.", "The new policy had a positive effect on sales.", "affect,effect,affected,effects", "affect"),
        ("Accept vs Except", "'Accept' is a verb meaning to receive or agree to. 'Except' is a preposition meaning excluding.", "I gladly accept your invitation to the party.", "Everyone arrived on time except for Mark.", "accept,except,accepting,excepted", "accept"),
        ("Loose vs Lose", "'Loose' is an adjective meaning not tight. 'Lose' is a verb meaning to misplace something or fail to win.", "Be careful not to lose your passport.", "This coat is very loose on me.", "lose,loose,lost,loosen", "lose"),
        ("Everyday vs Every day", "'Everyday' (one word) is an adjective meaning ordinary or daily ('everyday clothes'). 'Every day' (two words) is an adverbial phrase meaning each day.", "I practice speaking English every day.", "Stress has become part of everyday life.", "every day,everyday,everydays,each day", "every day"),
        ("Then vs Than", "'Then' refers to time, sequence, or consequence ('Do this, then that'). 'Than' is used exclusively in comparisons ('He is taller than me').", "She is much more experienced than I am.", "We ate dinner and then went for a walk.", "than,then,there,their", "than"),
        ("Their vs There vs They're", "'Their' shows possession ('their car'). 'There' indicates place or existence ('over there'). 'They're' is the contraction for 'they are'.", "They're going to present their new project over there.", "The students forgot their textbooks.", "They're,Their,There,Them", "They're"),
        ("Its vs It's", "'Its' is the possessive form ('The dog wagged its tail'). 'It's' is the contraction for 'it is' or 'it has'.", "It's going to be a wonderful afternoon.", "The company updated its privacy policy.", "It's,Its,It,Its'", "It's"),
        ("Your vs You're", "'Your' is a possessive pronoun ('your hat'). 'You're' is the contraction for 'you are'.", "You're going to love this new restaurant.", "Is this your umbrella left on the chair?", "You're,Your,You,Yours", "You're"),
        ("Whose vs Who's", "'Whose' shows possession ('Whose phone is ringing?'). 'Who's' is the contraction for 'who is' or 'who has'.", "Who's coming to the team lunch today?", "Whose jacket is this sitting on the bench?", "Who's,Whose,Who,Whos", "Who's"),
        ("Stationary vs Stationery", "'Stationary' means standing still or not moving. 'Stationery' refers to writing materials like paper and envelopes.", "The car remained stationary at the red light.", "I bought new letterheads and envelopes at the stationery shop.", "stationary,stationery,station,stationed", "stationary"),
        ("Principal vs Principle", "'Principal' means chief/main or a school head. 'Principle' refers to a fundamental rule, truth, or moral value.", "Honesty is an important moral principle.", "Mr. Davis is the principal of our high school.", "principle,principal,principles,principally", "principle"),
        ("Beside vs Besides", "'Beside' means by the side of or next to. 'Besides' means in addition to or moreover.", "She sat beside me during the conference.", "Besides reading, I enjoy cooking and painting.", "beside,besides,aside,next", "beside"),
        ("Among vs Between", "Use 'between' when referring to distinct, individual items (usually two). Use 'among' when referring to a group or collective body of three or more.", "The prize money was divided equally among the four winners.", "There is a secret agreement between the two directors.", "among,between,with,in", "among"),
        ("Sometime vs Sometimes vs Some time", "'Sometime' means at an unspecified time. 'Sometimes' means occasionally. 'Some time' means a span of time.", "We should get together for coffee sometime.", "Sometimes I like to listen to classical music.", "sometime,sometimes,some time,someway", "sometime"),
        ("Altogether vs All together", "'Altogether' means completely or entirely. 'All together' means gathered in one place as a group.", "It was an altogether enjoyable experience.", "We stood all together for the group photograph.", "altogether,all together,together,all", "altogether"),
        ("Compliment vs Complement", "'Compliment' means praise or admiration. 'Complement' means to complete or go well together with.", "The blue shirt complements your eyes perfectly.", "She paid me a nice compliment on my speech.", "complements,compliments,complement,complimented", "complements"),
        ("Emigrate vs Immigrate", "'Emigrate' means to leave one's home country. 'Immigrate' means to move into a new country to live permanently.", "They emigrated from Italy in 1995 and immigrated to Canada.", "My grandparents immigrated to America eighty years ago.", "immigrated,emigrated,migrated,moved", "immigrated"),
        ("Historic vs Historical", "'Historic' means famous, important, or monumental in history. 'Historical' means related to the study or facts of history.", "Signing the treaty was a historic moment.", "She loves reading historical novels set in the 18th century.", "historic,historical,historian,history", "historic"),
        ("Economic vs Economical", "'Economic' relates to the economy, finance, or trade. 'Economical' means thrifty, inexpensive, or avoiding waste.", "Buying energy-saving bulbs is very economical.", "The government announced new economic policies today.", "economical,economic,economy,economically", "economical"),
        ("Sensible vs Sensitive", "'Sensible' means reasonable, practical, or wise. 'Sensitive' means easily hurt, empathetic, or delicate.", "Wearing comfortable walking shoes was a very sensible decision.", "She is very sensitive to loud noises and bright lights.", "sensible,sensitive,sense,sensibly", "sensible"),
        ("Desert vs Dessert", "'Desert' (stress on first syllable) is an arid land; as a verb, it means to abandon. 'Dessert' (stress on second syllable) is a sweet dish.", "We had delicious chocolate cake for dessert.", "Camels can travel long distances across the desert.", "dessert,desert,deserts,desserts", "dessert"),
        ("Quiet vs Quite vs Quit", "'Quiet' means silent/peaceful. 'Quite' means fairly, completely, or very. 'Quit' means to stop or leave.", "Please be quiet while the test is taking place.", "The news was quite unexpected and surprising.", "quiet,quite,quit,quieter", "quiet"),
        ("Capital vs Capitol", "'Capital' refers to a chief city, uppercase letter, or financial wealth. 'Capitol' specifically refers to a building housing a legislature.", "Paris is the capital of France.", "The senators gathered inside the Capitol building.", "capital,capitol,capitals,capitols", "capital"),
        ("Continuous vs Continual", "'Continuous' means unbroken without any interruption. 'Continual' means occurring repeatedly over time with short breaks.", "The continuous noise from the highway kept me awake.", "His continual interruptions disturbed the whole presentation.", "continuous,continual,continually,continuously", "continuous"),
        ("Farther vs Further", "'Farther' refers to measurable physical distance. 'Further' refers to figurative distance, degree, or additional information.", "We need to discuss this topic further before deciding.", "How much farther is it to the top of the hill?", "further,farther,farthest,furthest", "further"),
        ("Fewer vs Less", "Use 'fewer' with plural countable nouns ('fewer books'). Use 'less' with singular uncountable nouns ('less sugar').", "There were fewer people at the meeting than expected.", "Please add less sugar to my tea.", "fewer,less,little,few", "fewer"),
        ("Number vs Amount", "Use 'number' with countable nouns ('a number of cars'). Use 'amount' with uncountable nouns ('an amount of money').", "A large number of students registered for the course.", "A significant amount of water was wasted.", "number,amount,quantity,total", "number"),
        ("Uninterested vs Disinterested", "'Uninterested' means not interested or bored. 'Disinterested' means impartial, unbiased, or neutral.", "A judge must remain completely disinterested in the trial outcome.", "He seemed totally uninterested in the conversation.", "disinterested,uninterested,interest,uninterestedly", "disinterested"),
        ("Person vs People vs Persons vs Peoples", "'Person' is singular. 'People' is plural for human beings. 'Persons' is formal legal usage. 'Peoples' refers to distinct ethnic groups or nations.", "Many people attended the cultural festival.", "She is a kind and thoughtful person.", "people,person,persons,peoples", "people"),
        ("Mastering Commonly Confused Words in Context", "Differentiating confusing word pairs significantly elevates your accuracy and authority in spoken and written English.", "Ensure you accept feedback and learn from every mistake.", "I need to ensure all details are correct.", "accept,except,affect,effect", "accept"),

        # Pronouns (130-139)
        ("Subject vs Object Pronouns Rules", "Use subject pronouns (I, he, she, we, they) as the doer of the verb. Use object pronouns (me, him, her, us, them) after verbs and prepositions.", "She and I went to the store together.", "The manager gave him and me the new assignment.", "I,me,myself,mine", "I"),
        ("Possessive Adjectives vs Possessive Pronouns", "Possessive adjectives (my, your, his, her, our, their) come before nouns. Possessive pronouns (mine, yours, his, hers, ours, theirs) stand alone.", "This hat is mine, and that coat is yours.", "Is this your notebook on the desk?", "mine,my,yours,your", "mine"),
        ("Reflexive & Emphatic Pronouns (Myself, Yourself)", "Use reflexive pronouns when the subject and object are the same person ('I hurt myself'). Use emphatic pronouns to add emphasis ('I completed it myself').", "I prepared the entire presentation myself.", "She looked at herself in the mirror.", "myself,herself,himself,yourselves", "myself"),
        ("Relative Pronouns: Who, Whom, Whose, Which, That", "Use 'who' for people (subject), 'whom' for people (object), 'whose' for possession, 'which' for things, and 'that' for essential clauses.", "The doctor who treated me was extremely kind.", "This is the camera that I bought last week.", "who,whom,whose,which", "who"),
        ("Defining vs Non-Defining Relative Clauses", "Defining clauses provide essential information (no commas). Non-defining clauses add extra details (separated by commas, 'which' or 'who', never 'that').", "My brother, who lives in Chicago, is a surgeon.", "The book that you recommended was brilliant.", "who,which,that,whom", "who"),
        ("Indefinite Pronouns: Someone, Anybody, Nothing, Everyone", "Indefinite pronouns refer to non-specific people or things. They take singular verbs ('Everyone is ready', NOT 'Everyone are ready').", "Everyone is excited about the upcoming holiday.", "Does anybody know the correct answer?", "is,are,were,be", "is"),
        ("Demonstrative Pronouns: This, That, These, Those", "Use 'this/these' for things near in time/space, and 'that/those' for distant things. Ensure singular/plural agreement with following verbs.", "These are my favorite pictures from the trip.", "That was a fantastic performance last night.", "These,This,Those,That", "These"),
        ("Reciprocal Pronouns: Each other vs One another", "Use 'each other' typically for two subjects, and 'one another' for three or more. Both express mutual action.", "The two rivals shook hands with each other.", "Team members supported one another throughout the project.", "each other,one another,themselves,myself", "each other"),
        ("Dummy Subject Pronouns: 'It' and 'There'", "Use 'it' for weather, time, distance, and identification ('It is raining'). Use 'there' to state existence ('There are three reasons').", "It is starting to rain heavily outside.", "There is a solution to every challenge.", "It,There,This,That", "It"),
        ("Common Pronoun Mistakes in Spoken Sentences", "Avoid saying 'Me and John went' (correct: 'John and I went') or 'Between you and I' (correct: 'Between you and me').", "John and I completed the report on time.", "This secret stays between you and me.", "I,me,myself,mine", "I"),

        # Gerunds & Infinitive Forms (140-142)
        ("Gerunds vs Infinitives: Enjoy swimming vs Want to swim", "Some verbs take a gerund (-ing) as an object (enjoy, avoid, suggest, consider). Other verbs take an infinitive (want, decide, hope, plan).", "I enjoy reading fiction in my free time.", "She decided to study medicine at university.", "reading,to read,read,reads", "reading"),
        ("Verbs Changing Meaning with Gerund or Infinitive", "Verbs like 'stop', 'remember', and 'forget' change meaning: 'stop to smoke' (pause in order to smoke) vs 'stop smoking' (quit the habit).", "I remember meeting him at the conference last year.", "Don't forget to lock the door when you leave.", "meeting,to meet,meet,met", "meeting"),
        ("Adjectives Ending in '-ed' vs '-ing'", "-ed adjectives describe how a person feels ('I am bored'). -ing adjectives describe the thing/situation causing the feeling ('The class is boring').", "I was fascinating by the story because the plot was interesting.", "She was bored during the long lecture.", "bored,boring,bores,bore", "bored"),

        # Used To / Be Used To / Get Used To (143-146)
        ("'Used To' + Base Verb for Past Habits & States", "Use 'used to + base verb' for past actions or states that are no longer true today. Negative: 'didn't use to'.", "I used to live in London when I was a student.", "Did you use to play basketball in school?", "used to,am used to,got used to,use to", "used to"),
        ("'Be Used To' + Gerund/Noun for Familiarity", "Use 'be used to + noun/gerund' when you are comfortable or accustomed to something ('I am used to waking up early').", "She is used to driving in heavy morning traffic.", "Are you used to the cold weather here yet?", "is used to,used to,got used to,uses to", "is used to"),
        ("'Get Used To' + Gerund/Noun for Adaptation Process", "Use 'get used to + noun/gerund' to describe the gradual process of becoming accustomed to something new.", "It took me a month to get used to working from home.", "You will soon get used to living in a big city.", "get used to,used to,be used to,uses to", "get used to"),
        ("Comparing Used To, Be Used To, and Get Used To", "Differentiate: 'I used to wake up late' (past habit), 'I am used to waking up early' (current state), 'I am getting used to waking up early' (ongoing process).", "I am getting used to speaking English every day.", "He used to smoke, but he quit last year.", "getting used to,used to,be used to,used for", "getting used to"),

        # Articles & Determiners (147-157)
        ("Indefinite Articles 'A' & 'An' Phonetic Rules", "Use 'a' before consonant sounds ('a university', 'a European') and 'an' before vowel sounds ('an hour', 'an honest person'). Base choice on sound, not spelling.", "He is an honest man who works at a university.", "It took an hour to arrive at the station.", "an,a,the,zero article", "an"),
        ("Definite Article 'The' Essential Guidelines", "Use 'the' when referring to specific items known to speaker and listener, unique things (the sun, the world), or superlatives (the best).", "The sun sets in the west.", "Could you pass me the salt on the table?", "The,A,An,Zero article", "The"),
        ("Zero Article: When NOT to Use Articles", "Do NOT use articles before general plural nouns, uncountable abstract concepts, names of languages, academic subjects, or meals.", "Knowledge is power.", "She is fluent in English and French.", "Knowledge,The knowledge,A knowledge,An knowledge", "Knowledge"),
        ("Quantifiers: 'Some' vs 'Any' Rules", "Use 'some' in positive affirmative sentences and polite offers/requests ('Would you like some tea?'). Use 'any' in negative sentences and general questions.", "Do you have any questions regarding the lesson?", "I bought some fresh apples at the market.", "any,some,no,many", "any"),
        ("Quantifiers: 'Much', 'Many', & 'A Lot Of'", "Use 'many' with countable nouns, 'much' with uncountable nouns (mostly in questions/negatives), and 'a lot of' with both in affirmative statements.", "There aren't many places left in the hall.", "How much time do we have before departure?", "many,much,a lot of,few", "many"),
        ("Quantifiers: 'Few' vs 'A Few' & 'Little' vs 'A Little'", "'A few' (countable) and 'a little' (uncountable) mean 'a small positive amount'. 'Few' and 'little' without 'a' have negative connotations meaning 'almost none'.", "I have a few friends in the city who can help us.", "There is little water left in the jug.", "a few,few,a little,little", "a few"),
        ("Determiners: 'Each' vs 'Every'", "'Each' considers items individually (usually two or more). 'Every' considers items collectively as a group (three or more). Both take singular verbs.", "Each student received an individual certificate.", "Every employee must attend the quarterly meeting.", "Each,Every,All,Both", "Each"),
        ("Determiners: 'Both', 'Either', & 'Neither'", "Use 'both' for two items together (plural verb). Use 'either' (one or the other) and 'neither' (not one nor the other) with singular verbs.", "Neither of the two candidates was selected.", "Both of my brothers live in Sydney.", "Neither,Either,Both,None", "Neither"),
        ("Determiners: 'All', 'Most', 'Whole', & 'No'", "Use 'all' with plural/uncountable nouns, 'whole' with singular countable nouns ('the whole day'), and 'no' before nouns to express zero quantity.", "I spent the whole day preparing for the exam.", "All students must submit their work today.", "whole,all,most,no", "whole"),
        ("Articles with Geographical Names & Places", "Use 'the' with oceans, rivers, mountain ranges, desert regions, and plural country names (the USA, the Netherlands). Omit articles for single mountains, lakes, or most countries.", "We sailed across the Mediterranean Sea.", "Mount Everest is the highest mountain in the world.", "the,a,an,zero article", "the"),
        ("Mastering Articles & Quantifiers in Fast Speech", "Proper article usage prevents unnatural pauses and makes your spoken English sound effortless and grammatically sound.", "She gave me a piece of useful advice.", "I need a few more minutes to complete this.", "a,an,the,some", "a"),

        # Punctuation (158-167)
        ("Capitalization Rules in Written English", "Capitalize proper nouns, days of the week, months, holidays, titles, national adjectives, and the first word of every sentence.", "We will visit Paris in July with Professor Smith.", "English and Spanish are widely spoken.", "Paris,paris,PARIS,Parisian", "Paris"),
        ("Comma Rules: Separating Clauses & Lists", "Use commas to separate items in a series, after introductory clauses, and before coordinating conjunctions connecting independent clauses.", "Although it was raining, we went for a walk.", "I bought apples, bananas, and oranges.", "raining,,raining,raining;,raining:", "raining,"),
        ("Apostrophe Rules: Possessives vs Contractions", "Use apostrophes for contractions (it's = it is) and possessives (dog's bone). Plural possessives place the apostrophe after the 's' (teachers' lounge).", "The children's playground was recently renovated.", "It's important to double-check your work.", "children's,childrens',childrens,children'es", "children's"),
        ("Quotation Marks & Direct Speech Formatting", "Enclose exact spoken words in quotation marks. Place commas and periods inside quotation marks in standard American formatting.", "She said, \"I will arrive at six o'clock.\"", "\"We are ready,\" declared the team lead.", "said,,said:,,said.,said;", "said,"),
        ("Semicolons & Colons Usage Masterclass", "Use a semicolon to join two closely related independent clauses without a conjunction. Use a colon to introduce a list, explanation, or quote.", "I have a big deadline tomorrow; therefore, I must stay up late.", "Please bring three items: a pen, a notebook, and a ruler.", ";,:,--,,", ";"),
        ("Hyphens & Dashes: Rules & Differences", "Use hyphens to connect compound adjectives before nouns ('a well-known author'). Use em-dashes (—) to emphasize or set off parenthetical thoughts.", "She is a well-known scientist in her field.", "This is a fast-growing tech company.", "well-known,well known,wellknow,well_known", "well-known"),
        ("Question Marks & Exclamation Marks Tone Control", "Use question marks only for direct questions (not indirect ones). Use exclamation marks sparingly to convey strong emotion or urgent commands.", "Where are you heading right now?", "What a wonderful surprise this is!", "?,!,.,,", "?"),
        ("Parentheses & Brackets in Professional Text", "Use parentheses () to add supplementary or non-essential commentary. Use square brackets [] for editorial clarifications within quotes.", "The event (which was held outdoors) was a great success.", "The report includes all recent figures.", "(which was held outdoors),{which was held outdoors},[which was held outdoors],<which was held outdoors>", "(which was held outdoors)"),
        ("Common Punctuation Traps in Written Communication", "Avoid comma splices (joining two independent sentences with only a comma). Use a semicolon or conjunction instead.", "I was hungry; however, I waited for my friend.", "She loves reading; she reads every night.", ";,.,,,:", ";"),
        ("Punctuation Pause Cues for Spoken Intonation", "Punctuation marks act as visual roadmap cues for natural speech pauses, breath management, and pitch changes.", "When the bell rang, everyone stood up immediately.", "In summary, we achieved all our goals.", "rang,,rang;,rang:,rang.", "rang,"),

        # Active & Passive Voice (168-171)
        ("Active vs Passive Voice Structural Comparison", "Active voice focuses on the performer of the action (Subject + Verb + Object). Passive voice focuses on the recipient of the action (Object + Be + Past Participle).", "The chef prepared a exquisite dinner.", "An exquisite dinner was prepared by the chef.", "was prepared,prepared,is preparing,has prepared", "was prepared"),
        ("Converting Present & Past Tenses to Passive", "Simple Present Passive: am/is/are + V3 ('Letters are delivered daily'). Simple Past Passive: was/were + V3 ('The house was built in 1990').", "The new bridge was opened by the mayor yesterday.", "Spanish is spoken in many countries.", "was opened,opened,is opening,has opened", "was opened"),
        ("Passive Voice with Modal Verbs (Can Be Done, Should Be Sent)", "Form passive modals using Modal + Be + Past Participle (e.g., 'The document must be signed before Friday').", "All applications must be submitted by midnight.", "This problem can be easily solved.", "must be submitted,must submit,must submitting,must have submit", "must be submitted"),
        ("Strategic Uses of Passive Voice in Formal & Technical English", "Use passive voice when the agent is unknown, obvious, or less important than the action itself, or in formal academic/business reports.", "The experiment was conducted under controlled laboratory conditions.", "Our server was updated last night.", "was conducted,conducted,conducts,is conducting", "was conducted"),

        # Phrasal Verbs (172-184)
        ("Understanding Phrasal Verbs & Separable Rules", "Phrasal verbs consist of a verb + particle (preposition or adverb). Separable phrasal verbs allow objects between verb and particle ('turn it on'). Pronouns MUST go in between.", "Please turn the lights off before leaving.", "Could you turn it on?", "turn off,turn off it,turn it off,turning off", "turn it off"),
        ("Essential Phrasal Verbs with 'Get'", "Master common 'get' phrasal verbs: get along with (have a good relationship), get over (recover from), get by (survive financially).", "She gets along very well with all her colleagues.", "It took him a month to get over the flu.", "gets along,gets over,gets by,gets up", "gets along"),
        ("Essential Phrasal Verbs with 'Take'", "Master 'take' phrasal verbs: take off (depart / remove), take over (assume control), take after (resemble a family member).", "The plane will take off in ten minutes.", "He takes after his father in demeanor.", "take off,take over,take after,take in", "take off"),
        ("Essential Phrasal Verbs with 'Turn'", "Master 'turn' phrasal verbs: turn down (refuse / lower volume), turn up (appear / increase volume), turn out (result in).", "She turned down the job offer because of the location.", "He surprisingly turned up at the party late.", "turned down,turned up,turned out,turned off", "turned down"),
        ("Essential Phrasal Verbs with 'Look'", "Master 'look' phrasal verbs: look after (care for), look forward to (anticipate eagerly + gerund), look up (search for information).", "I am really looking forward to visiting you next week.", "She looks after her younger brother.", "looking forward to,looking after,looking up,looking into", "looking forward to"),
        ("Essential Phrasal Verbs with 'Bring' & 'Give'", "Master: bring up (mention / raise a child), give up (stop trying / quit habit), give away (donate / reveal a secret).", "Don't give up on your dreams.", "She brought up an interesting point during the meeting.", "give up,bring up,give away,bring about", "give up"),
        ("Essential Phrasal Verbs with 'Come' & 'Go'", "Master: come across (find unexpectedly), come up with (invent/suggest an idea), go on (continue), go through (experience/examine).", "She came across an old photograph in the drawer.", "He came up with a brilliant business idea.", "came across,came up with,went on,went through", "came across"),
        ("Essential Phrasal Verbs with 'Put'", "Master: put off (postpone), put up with (tolerate), put on (wear / organize), put away (store in proper place).", "Never put off until tomorrow what you can do today.", "I cannot put up with this loud noise any longer.", "put off,put up with,put on,put away", "put off"),
        ("Essential Phrasal Verbs with 'Call' & 'Break'", "Master: call off (cancel), call back (return phone call), break down (stop functioning / become emotional), break out (start suddenly).", "The outdoor match was called off due to heavy rain.", "My car broke down on the highway this morning.", "called off,broke down,called back,broke out", "called off"),
        ("Business & Workplace Phrasal Verbs", "Master professional phrasal verbs: carry out (execute a plan), follow up on (check status), set up (arrange a meeting), wind up (conclude).", "We need to set up a conference call for tomorrow.", "Our team will carry out the project plan successfully.", "set up,carry out,follow up,wind up", "set up"),
        ("Three-Word Phrasal Verbs (Run out of, Catch up with)", "Three-word phrasal verbs are inseparable: run out of (deplete supply), catch up with (reach the same level), cut down on (reduce consumption).", "We have run out of printer paper.", "I need to catch up with my old friends.", "run out of,catch up with,cut down on,look up to", "run out of"),
        ("Phrasal Verbs for Travel, Socializing & Daily Life", "Master travel phrasal verbs: check in (register at hotel/airport), drop off (deliver someone), pick up (collect someone), see off (say goodbye at departure).", "My friend dropped me off at the airport terminal.", "We checked in at the hotel reception.", "dropped off,checked in,picked up,saw off", "dropped off"),
        ("Using Phrasal Verbs Naturally in Everyday Speech", "Using appropriate phrasal verbs makes your English sound conversational, warm, and natural rather than overly rigid.", "Let's catch up over lunch this afternoon.", "Hold on a minute while I grab my coat.", "catch up,hold on,carry on,keep up", "catch up"),

        # Word Formation & Structure (185-189)
        ("Prefixes & Suffixes: Rapid Vocabulary Expansion", "Prefixes (un-, dis-, re-, im-, mis-) change meaning. Suffixes (-ment, -tion, -able, -ful, -less) change word class (e.g., happy -> happiness).", "Her act of kindness brought great happiness to everyone.", "It is impossible to finish this without assistance.", "happiness,happy,happily,unhappy", "happiness"),
        ("Sentence Types: Simple, Compound & Complex Structures", "Simple sentences have one independent clause. Compound sentences join clauses with FANBOYS. Complex sentences use subordinating conjunctions.", "Although she was tired, she completed the report and sent it to her boss.", "He likes tea, but she prefers coffee.", "Complex,Simple,Compound,Compound-Complex", "Complex"),
        ("Word Order Rules: Subject-Verb-Object-Manner-Place-Time", "English standard word order follows SVOMPT (Subject + Verb + Object + Manner + Place + Time). E.g., 'He played tennis brilliantly at the park yesterday'.", "She played the piano beautifully at the concert last night.", "They drove carefully on the highway during the storm.", "played the piano beautifully,played beautifully the piano,beautifully played the piano,the piano played beautifully", "played the piano beautifully"),
        ("Direct vs Reported Speech (Backshifting Tenses)", "When reporting past speech, shift tenses back one step (Present Simple -> Past Simple; Present Continuous -> Past Continuous; Will -> Would).", "She said that she was working on a new project.", "He told us that he would call us back later.", "was working,is working,worked,has worked", "was working"),
        ("Inversion for Strong Emphasis in Formal Speech", "Place negative adverbs (Never, Seldom, Rarely, Hardly) at the start of a sentence followed by auxiliary inversion ('Never have I seen such beauty').", "Rarely have I heard such an inspiring presentation.", "Never will I forget this extraordinary trip.", "Rarely have I heard,Rarely I have heard,Rarely heard I,Rarely I heard", "Rarely have I heard"),

        # Writing Skills (190)
        ("Formal vs Informal Writing Styles & Email Etiquette", "Use formal language (no contractions, precise vocabulary, polite salutations) in business emails, and informal contractions in casual messages.", "Dear Sir/Madam, I am writing to inquire about the position.", "I look forward to hearing from you soon.", "inquire,ask,wonder,check", "inquire"),

        # Fluency & Vocabulary Building (191-202)
        ("Adverbs of Manner, Degree & Frequency", "Adverbs modify verbs, adjectives, or other adverbs. Adverbs of frequency (always, usually, often, seldom) go before main verbs but after 'be'.", "She always arrives at the office early.", "He spoke clearly and persuasively during the speech.", "always arrives,arrives always,is always arriving,always arrive", "always arrives"),
        ("Intensifiers: Very, Really, Extremely, Quite, Fairly", "Intensifiers modify the strength of adjectives. Extreme adjectives (fantastic, exhausted, freezing) take 'absolutely' rather than 'very'.", "I was absolutely exhausted after the marathon.", "The movie was extremely interesting.", "absolutely,very,fairly,quite", "absolutely"),
        ("Filler Words & Soft Hesitation in Natural Speech", "Native speakers use natural fillers (well, actually, you know, I mean, as a matter of fact) to gain thinking time without breaking conversation flow.", "Well, to be honest, I haven't considered that option yet.", "Actually, that makes a lot of sense.", "Well,Actually,You know,I mean", "Well"),
        ("Opinion Softeners: Politely Expressing Views", "Soften firm statements using phrases like 'In my opinion', 'It seems to me', 'From my perspective', or 'I would suggest'.", "In my opinion, we should focus on quality rather than speed.", "It seems to me that we need more data.", "In my opinion,It seems to me,From my view,I think", "In my opinion"),
        ("Expressing Agreement & Disagreement Gracefully", "Agree enthusiastically ('I completely agree', 'You're absolutely right'). Disagree politely ('I see your point, but...', 'I'm afraid I have a slightly different view').", "I see your point, but we must also consider the budget.", "I couldn't agree with you more.", "I see your point but,I completely agree,You are wrong,I disagree", "I see your point but"),
        ("Clarification Strategies in Live Conversations", "Ask for clarification politely: 'Could you rephrase that?', 'If I understand correctly, you mean...', 'Could you elaborate on that point?'", "Could you please elaborate on that last point?", "If I understand correctly, you want us to start today.", "elaborate,explain,say,tell", "elaborate"),
        ("Sentence Stress & Rhythm in Spoken English", "English is a stress-timed language. Content words (nouns, main verbs, adjectives) are stressed, while structure words (articles, prepositions, auxiliaries) are unstressed.", "We NEED to FOCUS on the MAIN OBJECTIVES.", "She WANTS to BUY a NEW CAR.", "NEED focus MAIN OBJECTIVES,We need to focus,on the main objectives,focus on main", "NEED focus MAIN OBJECTIVES"),
        ("Intonation Patterns: Rising vs Falling Tones", "Use falling intonation for statements and Wh- questions. Use rising intonation for Yes/No questions and checking understanding.", "Are you coming with us tonight? (Rising)", "Where do you live? (Falling)", "Rising,Falling,Flat,Monotone", "Rising"),
        ("Connected Speech: Linking Consonant to Vowel Sounds", "When a word ends in a consonant and the next starts with a vowel, link them smoothly (e.g., 'an apple' sounds like 'a-napple'; 'check it out' sounds like 'che-ki-tout').", "Can you check it out for me?", "He held an apple in his hand.", "check it out,check-it-out,check_it_out,checkitout", "check it out"),
        ("Connected Speech: Elision & Blending Sounds", "In fast natural speech, sounds drop or blend ('next door' -> 'nexdoor'; 'must be' -> 'musbe'; 'going to' -> 'gonna').", "He must be arriving any minute now.", "They are going to launch the product tomorrow.", "must be,gonna,wanna,gotcha", "must be"),
        ("Expanding Academic & Business Vocabulary", "Replace weak words with precise formal alternatives: 'big' -> 'substantial'; 'change' -> 'modify'; 'help' -> 'assist'; 'show' -> 'demonstrate'.", "The team demonstrated a substantial improvement in performance.", "We need to modify our marketing strategy.", "substantial,big,huge,large", "substantial"),
        ("Mastering Idiomatic Expressions for Everyday Fluency", "Idioms add color to speech. Examples: 'hit the nail on the head' (be exactly right), 'piece of cake' (very easy), 'break the ice' (initiate conversation).", "His explanation hit the nail on the head.", "The exam turned out to be a piece of cake.", "hit the nail on the head,piece of cake,break the ice,under the weather", "hit the nail on the head"),

        # Adjectives — Order & Multiple Adjectives (203-204)
        ("Adjective Order Rules (OSASCOMP Formula)", "When using multiple adjectives before a noun, follow OSASCOMP: Opinion, Size, Age, Shape, Color, Origin, Material, Purpose (e.g., 'a beautiful small old round wooden table').", "She wore an elegant long red silk dress to the gala.", "He bought a charming antique Italian leather jacket.", "elegant long red silk,red long elegant silk,silk red long elegant,long red silk elegant", "elegant long red silk"),
        ("Comparative & Superlative Adjectives Rules", "Short adjectives take -er/-est ('faster', 'fastest'). Long adjectives take more/most ('more beautiful'). Irregulars: good/better/best, bad/worse/worst, far/further/furthest.", "This is the most challenging project I have ever undertaken.", "She is much taller than her sister.", "most challenging,more challenging,challengingest,most challenge", "most challenging"),

        # General Grammar Rules & Common Mistakes (205-217)
        ("Subject-Verb Agreement: Singular & Plural Harmony", "Ensure singular subjects take singular verbs and plural subjects take plural verbs, even when long prepositional phrases separate them.", "The box of colorful crayons is sitting on the desk.", "The students in the classroom are listening attentively.", "is,are,were,be", "is"),
        ("Subject-Verb Agreement with Collective Nouns", "Collective nouns (team, committee, family, jury) take singular verbs in American English when acting as a unified unit ('The committee has reached a consensus').", "The committee has announced its final decision.", "My family is planning a summer vacation.", "has,have,are,were", "has"),
        ("Avoiding Double Negatives in Spoken English", "Double negatives ('I don't know nothing') are grammatically incorrect in standard English. Express negative ideas with a single negative word ('I don't know anything').", "I don't know anything about that issue.", "She didn't see anybody at the park.", "anything,nothing,something,everything", "anything"),
        ("Correct Pronoun Alignment in Complex Contexts", "Match pronouns in gender and number with their antecedents. E.g., 'Every student must submit their (or his/her) project on time'.", "Every employee should double-check their schedule.", "Each member brought her own equipment.", "their,his,her,its", "their"),
        ("Dangling & Misplaced Modifiers and How to Fix Them", "Modifiers must clearly attach to the noun they describe. Avoid dangling modifiers ('Walking down the street, the trees were pretty' -> 'Walking down the street, I saw pretty trees').", "Walking down the street, I noticed a new coffee shop.", "Having finished the report, she turned off her computer.", "Walking down the street I noticed,Walking down the street the shop noticed,Walking the street noticed I,Noticed walking street", "Walking down the street I noticed"),
        ("Parallel Structure in Lists & Sentences", "Maintain consistent grammatical forms when listing items or connecting ideas with conjunctions (e.g., 'He likes swimming, running, and cycling', NOT 'and to cycle').", "She enjoys swimming, hiking, and reading historical novels.", "He promised to study hard, practice daily, and attend all lectures.", "hiking,to hike,hiked,hikes", "hiking"),
        ("Avoiding Redundancy & Wordy Expressions", "Eliminate redundant phrasing in professional communication: 'repeat again' -> 'repeat'; 'close proximity' -> 'close'; 'added bonus' -> 'bonus'.", "Please repeat the instructions clearly.", "The office is in close proximity to the station.", "repeat,repeat again,repeat back,re-repeat", "repeat"),
        ("Preposition Mistakes to Eliminate in Conversation", "Common errors: 'discuss about' (wrong -> 'discuss'), 'married with' (wrong -> 'married to'), 'listen music' (wrong -> 'listen to music').", "She has been married to my uncle for twenty years.", "Let's listen to some relaxing music.", "married to,married with,married by,married at", "married to"),
        ("Tense Consistency Across Paragraphs & Narratives", "Do not shift abruptly between past and present tenses without a logical reason when telling a story or describing a process.", "He walked into the room, sat down on the chair, and opened his book.", "When she arrived, everyone welcomed her warmly.", "opened,opens,opening,has opened", "opened"),
        ("Common Grammar Traps in Job Interviews", "Use strong past tense action verbs for achievements ('managed', 'implemented', 'orchestrated') and avoid overly tentative language.", "I successfully managed a team of ten software developers.", "She spearheaded the new marketing campaign.", "managed,was managing,manage,have managed", "managed"),
        ("Spoken vs Written English Grammar Conventions", "Recognize when informal contractions and reduced forms are acceptable (spoken/casual text) versus when full forms are required (formal writing).", "We are writing to confirm your reservation details.", "I'm gonna leave soon.", "We are,We're,Us are,We'm", "We are"),
        ("Comprehensive Grammar Diagnostic Checkup", "Test your overall accuracy across modal verbs, tenses, prepositions, articles, and sentence structure.", "If I had known about the change, I would have informed you.", "Although it was late, we finished the task.", "would have informed,will inform,had informed,informed", "would have informed"),
        ("Final Grammar Mastery Review & Assessment", "Consolidate all 234 grammar rules and speaking drills to ensure natural confidence, clear pronunciation, and high structural accuracy.", "Consistent daily practice is the key to mastering English fluency.", "I am confident in my ability to speak English clearly.", "is,are,were,be", "is"),

        # Descriptive Vocabulary, Idioms & Conversation (218-229)
        ("Idioms for Emotions: Happiness, Anger & Stress", "Learn expressive idioms: 'on cloud nine' (ecstatically happy), 'see red' (become furious), 'under the weather' (slightly unwell), 'over the moon' (delighted).", "She was on cloud nine when she received the promotion.", "I am feeling a bit under the weather today.", "on cloud nine,under the weather,seeing red,over the moon", "on cloud nine"),
        ("Idioms for Work, Success & Perseverance", "Learn workplace idioms: 'hit the ground running' (start with immediate energy), 'learn the ropes' (master new tasks), 'burn the midnight oil' (work late into night).", "It took me a few weeks to learn the ropes at my new job.", "We had to burn the midnight oil to meet the deadline.", "learn the ropes,burn the midnight oil,hit the ground running,cut corners", "learn the ropes"),
        ("Idioms for Money, Decisions & Opportunities", "Learn decision idioms: 'at a crossroads' (facing a big choice), 'break the bank' (cost too much), 'bite the bullet' (face a tough situation bravely).", "We decided to bite the bullet and invest in new equipment.", "This vacation won't break the bank.", "bite the bullet,break the bank,at a crossroads,cost an arm and a leg", "bite the bullet"),
        ("Descriptive Adjectives for Character & Personality", "Expand personality vocabulary: meticulous (paying great attention to detail), charismatic (inspiring charm), compassionate (kind/empathetic), reliable (trustworthy).", "Our team lead is exceptionally meticulous in her work.", "He is a compassionate and reliable friend.", "meticulous,charismatic,compassionate,reliable", "meticulous"),
        ("Descriptive Vocabulary for Atmosphere & Settings", "Describe places vividly: tranquil (peaceful/calm), bustling (full of noisy activity), picturesque (charming like a picture), vibrant (energetic/colorful).", "We spent the weekend in a tranquil mountain village.", "The bustling city market was filled with vivid colors.", "tranquil,bustling,picturesque,vibrant", "tranquil"),
        ("Strong Verbs vs Weak Verbs with 'Very'", "Upgrade your vocabulary by replacing 'very + weak verb/adjective': 'very tired' -> 'exhausted'; 'very clean' -> 'spotless'; 'very cold' -> 'freezing'; 'very smart' -> 'brilliant'.", "After the long hike, we were completely exhausted.", "The kitchen was sparkling and spotless.", "exhausted,spotless,freezing,brilliant", "exhausted"),
        ("Collocations with 'Take', 'Have', & 'Make'", "Natural verb-noun pairings: take a break, take a risk, have a conversation, have an impact, make progress, make an effort.", "We made significant progress during today's brainstorming session.", "Let's take a quick fifteen-minute break.", "made progress,took progress,had progress,did progress", "made progress"),
        ("Collocations with 'Do', 'Pay', & 'Keep'", "Natural verb-noun pairings: do business, do a favor, pay attention, pay a compliment, keep a promise, keep in mind.", "Please keep in mind that the deadline is tomorrow.", "You should pay close attention to the instructions.", "keep in mind,pay attention,do a favor,keep a promise", "keep in mind"),
        ("Expressing Sympathy, Congratulations & Praise", "Conversational phrases: 'I am so sorry for your loss', 'Warmest congratulations on your achievement!', 'Kudos to you for a job well done!'", "Warmest congratulations on your remarkable promotion!", "I am so sorry to hear about your bad news.", "Warmest congratulations,I am sorry,Kudos,Well done", "Warmest congratulations"),
        ("Making Proposals, Suggestions & Recommendations", "Use persuasive phrases: 'How about we try...?', 'I strongly recommend that we...', 'Why don't we consider...?', 'It might be beneficial to...'", "I strongly recommend that we test the software thoroughly.", "How about we schedule the meeting for Tuesday morning?", "strongly recommend,how about,why don't we,I suggest", "strongly recommend"),
        ("Summarizing & Paraphrasing Ideas in Speaking", "Useful transition phrases: 'To sum up...', 'In short...', 'To put it another way...', 'In essence, what I am saying is...'", "To sum up, our strategy focuses on quality and customer satisfaction.", "In short, we need to increase our efficiency.", "To sum up,In short,In essence,To put it another way", "To sum up"),
        ("Storytelling Techniques & Narrative Connectors", "Engage listeners with narrative markers: 'It all started when...', 'Out of nowhere...', 'To make a long story short...', 'Before I knew it...'", "To make a long story short, we landed safely and enjoyed the trip.", "Out of nowhere, a heavy downpour started.", "To make a long story short,Out of nowhere,Before I knew it,It all started when", "To make a long story short"),

        # Nouns (230-232)
        ("Countable vs Uncountable Nouns & Tricky Nouns", "Uncountable nouns (information, advice, furniture, luggage, equipment, news) never take 'a/an' and do not add '-s'. Use 'a piece of' to count them.", "He gave me a valuable piece of advice.", "All the news was positive.", "piece of advice,advices,an advice,many advices", "piece of advice"),
        ("Plural Noun Rules & Irregular Plurals", "Irregular plurals change vowels (man -> men, tooth -> teeth, mouse -> mice) or stay identical (sheep, deer, fish). Always-plural nouns take plural verbs (pants, scissors, glasses).", "Where are my reading glasses?", "The sheep were grazing peacefully in the field.", "glasses,glass,pair of glass,glasse", "glasses"),
        ("Compound Nouns & Possessive Nouns Rules", "Possessive 's is used for people and animals ('John's car'). Use 'of' for inanimate objects ('the roof of the house'). Compound nouns form plurals on the head noun ('passers-by').", "The roof of the house was damaged in the storm.", "They are my brother's close friends.", "roof of the house,house's roof,roof house,roofs house", "roof of the house"),

        # Causative Verbs & Suggest (233-234)
        ("Causative Verbs: Make, Have, Get, Let, & Help", "Structure rules: Make/Have/Let + object + base verb ('She made me clean'); Get + object + to-infinitive ('I got him to repair the sink'); Help takes base verb or to-infinitive.", "She made me rewrite the entire essay.", "I will get the mechanic to check the brakes.", "made me rewrite,made me to rewrite,got me rewrite,let me to rewrite", "made me rewrite"),
        ("Using 'Suggest' & 'Recommend' Correctly", "Never say 'suggest me to do'. Correct structures: 'suggest + -ing' ('I suggest taking a break') OR 'suggest that + subject + base verb' ('I suggest that he take a break').", "I suggest taking a short walk to clear your head.", "The doctor recommended that she rest for three days.", "taking,to take,that he takes,him to take", "taking")
    ]

    # Map subtopics into items 32 to 234
    for idx, (title, exp, ex, sp, opt, corr) in enumerate(subtopic_db):
        item_index = 61 + idx  # 61 to 234
        if item_index > 234:
            break
        
        # Get category and id from asset_items
        asset_obj = asset_items[item_index - 32] # 0-indexed into asset_items
        
        item = {
            "id": asset_obj["id"],
            "category": asset_obj.get("category", "Useful Lessons"),
            "level": "Intermediate" if item_index <= 160 else "Advanced",
            "title": f"Lesson {item_index} — {title}",
            "explanation": exp,
            "exampleText": f"Example: {ex}",
            "speechPrompt": sp,
            "optionsString": opt,
            "correctOption": corr,
            "orderIndex": item_index
        }
        final_lessons.append(item)

    print(f"Total compiled lessons: {len(final_lessons)}")
    
    # Also include the lesson specs (32 to 60) in position
    # Let's insert lesson_specs correctly into final_lessons!
    # Let's re-build final_lessons clean:
    clean_lessons = list(existing_lessons[:31]) # Lessons 1 to 31
    clean_lessons.extend(lesson_specs) # Lessons 32 to 60

    # Add remaining 61 to 234
    for idx, (title, exp, ex, sp, opt, corr) in enumerate(subtopic_db):
        item_index = 61 + idx
        if item_index > 234:
            break
        asset_obj = existing_lessons[item_index - 1]
        item = {
            "id": asset_obj["id"],
            "category": asset_obj.get("category", "Useful Lessons"),
            "level": "Intermediate" if item_index <= 160 else "Advanced",
            "title": f"Lesson {item_index} — {title}",
            "explanation": exp,
            "exampleText": f"Example: {ex}",
            "speechPrompt": sp,
            "optionsString": opt,
            "correctOption": corr,
            "orderIndex": item_index
        }
        clean_lessons.append(item)

    print(f"Clean lessons total: {len(clean_lessons)}")
    
    # Save back to useful_lessons.json
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(clean_lessons, f, indent=2, ensure_ascii=False)

    print("Successfully updated useful_lessons.json!")

if __name__ == "__main__":
    generate_lessons()
