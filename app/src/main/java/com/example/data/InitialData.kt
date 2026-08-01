package com.example.data

object InitialData {

    val grammarLessons = listOf(
        // Beginner Level Lessons
        GrammarLesson(
            id = 1,
            title = "Contractions: Speaking Faster",
            level = "Beginner",
            explanation = "Native English speakers rarely say 'I am' or 'You are' in casual conversation. Instead, they use contractions like 'I'm', 'you're', 'he's', and 'they're' to speak faster and sound more natural.",
            exampleText = "I'm going to the grocery store. Do you want to join?",
            exampleTranslation = "",
            optionsString = "I am,I'm,I'd,I'll",
            correctOption = "I'm",
            speechPrompt = "I'm free tonight and I'm ready to practice speaking.",
            orderIndex = 1
        ),
        GrammarLesson(
            id = 2,
            title = "Wanna & Gonna: Future Intentions",
            level = "Beginner",
            explanation = "In informal speech, 'want to' becomes 'wanna' and 'going to' (followed by a verb) becomes 'gonna'. These are connected speech patterns used everywhere in daily life.",
            exampleText = "I'm gonna grab some coffee because I wanna stay awake.",
            exampleTranslation = "",
            optionsString = "going to / want to,gonna / wanna,go / want,gonna / want to",
            correctOption = "gonna / wanna",
            speechPrompt = "I'm gonna learn English and I wanna speak fluently.",
            orderIndex = 2
        ),
        GrammarLesson(
            id = 3,
            title = "Connected Speech: 'Gotcha' and 'Betcha'",
            level = "Beginner",
            explanation = "In spoken English, 'got you' and 'bet you' are shortened into 'gotcha' and 'betcha'. This makes your speech sound very conversational and natural.",
            exampleText = "Gotcha! I will pick up some milk on my way home.",
            exampleTranslation = "",
            optionsString = "got you,gotcha,got,get you",
            correctOption = "gotcha",
            speechPrompt = "I betcha we will have a great time at the park today.",
            orderIndex = 3
        ),
        GrammarLesson(
            id = 4,
            title = "The Soft 'T' sound in 'Water' and 'Better'",
            level = "Beginner",
            explanation = "In American English, a 't' sound between vowels is pronounced like a soft 'd' or 'flap t' sound. This makes words like 'water' and 'better' flow much smoother.",
            exampleText = "The water is better if it is cold.",
            exampleTranslation = "",
            optionsString = "better,bet-ter,bedder,best",
            correctOption = "better",
            speechPrompt = "I bought a little butter but the butter was bitter.",
            orderIndex = 4
        ),
        GrammarLesson(
            id = 5,
            title = "Connected Speech: 'Gimme' and 'Lemme'",
            level = "Beginner",
            explanation = "In informal, rapid speech, 'give me' simplifies to 'gimme' and 'let me' simplifies to 'lemme'. These contractions are extremely common in daily conversations.",
            exampleText = "Lemme know when you are ready to leave.",
            exampleTranslation = "",
            optionsString = "let me,lemme,let's,let",
            correctOption = "lemme",
            speechPrompt = "Gimme a second to grab my shoes and I'll meet you.",
            orderIndex = 5
        ),
        GrammarLesson(
            id = 6,
            title = "Reductions: 'Kind of' to 'Kinda'",
            level = "Beginner",
            explanation = "In casual conversation, 'kind of' is spoken as 'kinda' and 'sort of' as 'sorta'. Use these when you want to soften a statement or express uncertainty.",
            exampleText = "I'm kinda tired so I might stay in tonight.",
            exampleTranslation = "",
            optionsString = "kind of,kinda,kind,kinda of",
            correctOption = "kinda",
            speechPrompt = "It's sorta raining but we can still go out.",
            orderIndex = 6
        ),

        // Intermediate Level Lessons
        GrammarLesson(
            id = 7,
            title = "Connected Speech: 'Didja' and 'Wouldja'",
            level = "Intermediate",
            explanation = "When 'Did you' or 'Would you' are spoken quickly, the 'd' and 'y' sounds merge into a 'j' sound, creating 'Didja' /dɪdʒə/ or 'Wouldja' /wʊdʒə/.",
            exampleText = "Didja have a chance to finish that report yet?",
            exampleTranslation = "",
            optionsString = "Did you,Didja,Do you,Would you",
            correctOption = "Didja",
            speechPrompt = "Wouldja mind opening the window for some fresh air?",
            orderIndex = 7
        ),
        GrammarLesson(
            id = 8,
            title = "Softening Demands: 'Could you...'",
            level = "Intermediate",
            explanation = "To sound polite in spoken English, we use modal helping verbs like 'Could you...' or 'Would you mind...' instead of commanding 'Give me' or 'Do this'.",
            exampleText = "Could you please pass me the salt?",
            exampleTranslation = "",
            optionsString = "Give me,Could you,Can you,Do you",
            correctOption = "Could you",
            speechPrompt = "Could you tell me how to get to the nearest station?",
            orderIndex = 8
        ),
        GrammarLesson(
            id = 9,
            title = "Softening Bad News: 'I'm afraid...'",
            level = "Intermediate",
            explanation = "To deliver bad news or a refusal politely, native speakers use 'I'm afraid...' instead of directly saying 'No' or 'We can't'. It shows empathy and softens the impact.",
            exampleText = "I'm afraid we are completely sold out of tickets.",
            exampleTranslation = "",
            optionsString = "I'm sorry,I'm afraid,No,I fear",
            correctOption = "I'm afraid",
            speechPrompt = "I'm afraid I won't be able to make it to the party.",
            orderIndex = 9
        ),
        GrammarLesson(
            id = 10,
            title = "Linking Consonant to Vowel",
            level = "Intermediate",
            explanation = "When a word ends in a consonant and the next word starts with a vowel, fluent speakers link them together so they sound like one word. For example, 'hold on' sounds like 'hol-don'.",
            exampleText = "Could you hold on for just a minute?",
            exampleTranslation = "",
            optionsString = "hold on,hol-don,hold,on",
            correctOption = "hold on",
            speechPrompt = "Turn it off and turn it back on again.",
            orderIndex = 10
        ),
        GrammarLesson(
            id = 11,
            title = "The Glottal Stop 'T' sound",
            level = "Intermediate",
            explanation = "In many English accents, a 't' before an 'n' sound is not fully released. Instead, the throat closes briefly, creating a 'glottal stop'. This is heard in words like 'button' and 'mountain'.",
            exampleText = "It's important to climb the mountain safely.",
            exampleTranslation = "",
            optionsString = "important,impor-tant,imporden,import",
            correctOption = "important",
            speechPrompt = "The button on my shirt is missing.",
            orderIndex = 11
        ),

        // Advanced Level Lessons
        GrammarLesson(
            id = 12,
            title = "Active Present: Using the '-ing' Drop",
            level = "Advanced",
            explanation = "In relaxed conversations, the 'g' in '-ing' words is often dropped and replaced with an apostrophe (e.g., 'runnin'', 'talkin'', 'somethin''). It makes speech flow smoothly.",
            exampleText = "What're you workin' on these days?",
            exampleTranslation = "",
            optionsString = "working,workin',work,works",
            correctOption = "workin'",
            speechPrompt = "I'm packin' my bags because I'm travelin' tomorrow.",
            orderIndex = 12
        ),
        GrammarLesson(
            id = 13,
            title = "Connected Speech: 'Shoulda, Woulda, Coulda'",
            level = "Advanced",
            explanation = "In rapid spoken English, past modals like 'should have', 'would have', and 'could have' are reduced to 'shoulda', 'woulda', and 'coulda' to express regrets quickly.",
            exampleText = "I shoulda called you sooner but I forgot my phone.",
            exampleTranslation = "",
            optionsString = "should have,shoulda,should,should of",
            correctOption = "shoulda",
            speechPrompt = "We coulda won the game if we had practiced more.",
            orderIndex = 13
        ),
        GrammarLesson(
            id = 14,
            title = "Inversion for Emphasis",
            level = "Advanced",
            explanation = "To emphasize a negative or limiting condition, fluent speakers invert the subject and auxiliary verb after words like 'rarely', 'seldom', or 'never'. For example: 'Rarely do I see...'",
            exampleText = "Rarely have I seen such a beautiful sunset.",
            exampleTranslation = "",
            optionsString = "Rarely I have seen,Rarely have I seen,Never I saw,Hardly I did see",
            correctOption = "Rarely have I seen",
            speechPrompt = "Under no circumstances should you open that door.",
            orderIndex = 14
        ),
        GrammarLesson(
            id = 15,
            title = "Euphemisms: Softening Harsh Realities",
            level = "Advanced",
            explanation = "Advanced English speakers use euphemisms to speak about difficult topics politely. Instead of saying someone 'died' or was 'fired', they say 'passed away' or was 'let go'.",
            exampleText = "She was let go due to the company's restructuring.",
            exampleTranslation = "",
            optionsString = "fired,let go,dismissed,quit",
            correctOption = "let go",
            speechPrompt = "We should break the news gently to them.",
            orderIndex = 15
        )
    )

    val conversations = listOf(
        ConversationSet(
            id = 1,
            title = "Ordering Hot Coffee",
            scenario = "In a bustling downtown coffee shop, Sarah is ordering a morning beverage from a friendly barista named Alex.",
            basicDialogueJson = """[{"speaker": "Alex", "text": "Good morning! Welcome to Brew Haven. What can I get for you today?", "role": "A"}, {"speaker": "Sarah", "text": "Hi! I'd like a small coffee, please.", "role": "B"}, {"speaker": "Alex", "text": "Hot or iced?", "role": "A"}, {"speaker": "Sarah", "text": "Hot, please. With milk.", "role": "B"}, {"speaker": "Alex", "text": "That will be three dollars.", "role": "A"}, {"speaker": "Sarah", "text": "Here you go. Thank you!", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Alex", "text": "Good morning! Welcome to Brew Haven. What can I get started for you today?", "role": "A"}, {"speaker": "Sarah", "text": "Hi! Can I please get a medium vanilla latte with oat milk?", "role": "B"}, {"speaker": "Alex", "text": "You got it! Hot or iced?", "role": "A"}, {"speaker": "Sarah", "text": "Hot, please. And could I also get one of those chocolate croissants in the display case?", "role": "B"}, {"speaker": "Alex", "text": "Sure thing! Do you want that warmed up?", "role": "A"}, {"speaker": "Sarah", "text": "Yes, please! That would be lovely.", "role": "B"}, {"speaker": "Alex", "text": "Perfect. Your total is eight dollars and fifty cents. Tap your card whenever you are ready.", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Alex", "text": "Welcome back to Brew Haven! Shall I set you up with your usual single-origin pour-over today?", "role": "A"}, {"speaker": "Sarah", "text": "Actually, I'd love to try your seasonal dark roast espresso with a splash of oat milk and half-sweet vanilla syrup.", "role": "B"}, {"speaker": "Alex", "text": "Excellent choice! Would you care to pair that with a freshly baked almond croissant toasted to perfection?", "role": "A"}, {"speaker": "Sarah", "text": "That sounds delightful. Could you also pack a double espresso in a travel mug for my meeting downtown?", "role": "B"}, {"speaker": "Alex", "text": "Absolutely! I'll prepare both right away. Your total comes to twelve dollars and seventy-five cents.", "role": "A"}, {"speaker": "Sarah", "text": "Perfect, tapping my card now. Thanks as always, Alex!", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Alex", "text": "Good morning! Welcome to Brew Haven. What can I get started for you today?", "role": "A"}, {"speaker": "Sarah", "text": "Hi! Can I please get a medium vanilla latte with oat milk?", "role": "B"}, {"speaker": "Alex", "text": "You got it! Hot or iced?", "role": "A"}, {"speaker": "Sarah", "text": "Hot, please. And could I also get one of those chocolate croissants in the display case?", "role": "B"}, {"speaker": "Alex", "text": "Sure thing! Do you want that warmed up?", "role": "A"}, {"speaker": "Sarah", "text": "Yes, please! That would be lovely.", "role": "B"}, {"speaker": "Alex", "text": "Perfect. Your total is eight dollars and fifty cents. Tap your card whenever you are ready.", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Latte (espresso with milk), Oat Milk (dairy alternative), Display Case (glass container showing food)",
            comprehensionQuestion = "What kind of milk did Sarah request for her latte?",
            comprehensionOptions = "Whole Milk,Almond Milk,Oat Milk,Soy Milk",
            comprehensionAnswer = "Oat Milk"
        ),
        ConversationSet(
            id = 2,
            title = "The Job Interview",
            scenario = "A software engineer named David is interviewing with a hiring manager, Elena, for a Senior Mobile Developer position.",
            basicDialogueJson = """[{"speaker": "Elena", "text": "Hello David, nice to meet you. Please take a seat.", "role": "A"}, {"speaker": "David", "text": "Thank you, Elena. I'm excited to be here today.", "role": "B"}, {"speaker": "Elena", "text": "Can you tell me about your work experience?", "role": "A"}, {"speaker": "David", "text": "I am a mobile app developer. I have worked on Android apps for five years.", "role": "B"}, {"speaker": "Elena", "text": "That sounds great. Why do you want to join our company?", "role": "A"}, {"speaker": "David", "text": "Your team builds awesome products, and I want to help create great apps with you.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Elena", "text": "Thanks for coming in, David. To start off, could you tell me a bit about your background in mobile development?", "role": "A"}, {"speaker": "David", "text": "Certainly! I've been developing native Android apps for about five years, primarily focusing on clean architecture and Jetpack Compose.", "role": "B"}, {"speaker": "Elena", "text": "That's great. We use Compose heavily here. How do you handle complex state management in larger projects?", "role": "A"}, {"speaker": "David", "text": "I prefer using UI State holders with StateFlow, driven by ViewModels, backed by local offline caches using Room.", "role": "B"}, {"speaker": "Elena", "text": "Impressive answer. What is your strategy for handling tight deadlines or shifting requirements?", "role": "A"}, {"speaker": "David", "text": "I focus on incremental development and keeping communication open. I'd rather raise risks early than miss a milestone.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Elena", "text": "Good afternoon, David. Beyond technical fluency, how do you approach architectural design tradeoffs when scaling mobile applications?", "role": "A"}, {"speaker": "David", "text": "I prioritize modularity and separation of concerns using clean architecture, leveraging reactive flows to decouple state from side effects.", "role": "B"}, {"speaker": "Elena", "text": "Excellent. When cross-functional requirements conflict under tight quarter deadlines, how do you align engineering priorities with product leadership?", "role": "A"}, {"speaker": "David", "text": "I establish transparent risk matrices early, propose incremental phased deliverables, and ensure continuous automated testing to maintain quality without sacrificing momentum.", "role": "B"}, {"speaker": "Elena", "text": "That level of strategic alignment is exactly what our engineering organization needs. Let's discuss your leadership vision for the engineering squad.", "role": "A"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Elena", "text": "Thanks for coming in, David. To start off, could you tell me a bit about your background in mobile development?", "role": "A"}, {"speaker": "David", "text": "Certainly! I've been developing native Android apps for about five years, primarily focusing on clean architecture and Jetpack Compose.", "role": "B"}, {"speaker": "Elena", "text": "That's great. We use Compose heavily here. How do you handle complex state management in larger projects?", "role": "A"}, {"speaker": "David", "text": "I prefer using UI State holders with StateFlow, driven by ViewModels, backed by local offline caches using Room.", "role": "B"}, {"speaker": "Elena", "text": "Impressive answer. What is your strategy for handling tight deadlines or shifting requirements?", "role": "A"}, {"speaker": "David", "text": "I focus on incremental development and keeping communication open. I'd rather raise risks early than miss a milestone.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "StateFlow (reactive stream), Room (SQLite database wrapper), Milestones (key deadlines in project management)",
            comprehensionQuestion = "How many years of experience does David have in mobile development?",
            comprehensionOptions = "2 Years,3 Years,5 Years,10 Years",
            comprehensionAnswer = "5 Years"
        ),
        ConversationSet(
            id = 3,
            title = "A Friendly Cafe Catch-up",
            scenario = "Two college friends, Emily and Michael, run into each other at a quiet bookstore cafe after several months.",
            basicDialogueJson = """[{"speaker": "Emily", "text": "Hi Michael! Long time no see!", "role": "A"}, {"speaker": "Michael", "text": "Emily! Wow, good to see you! How are you?", "role": "B"}, {"speaker": "Emily", "text": "I'm good! I started a new job last week.", "role": "A"}, {"speaker": "Michael", "text": "Congratulations! What kind of job is it?", "role": "B"}, {"speaker": "Emily", "text": "I'm a graphic designer now. How about you?", "role": "A"}, {"speaker": "Michael", "text": "I'm still playing music in my band. We have a show this Friday!", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Emily", "text": "Oh my gosh, Michael? Is that you? It's been ages!", "role": "A"}, {"speaker": "Michael", "text": "Emily! Wow, what a surprise! Yes, it's me. How have you been?", "role": "B"}, {"speaker": "Emily", "text": "I've been good! Busy, but good. I actually started a new graphic design job last month.", "role": "A"}, {"speaker": "Michael", "text": "No way, congratulations! That's your dream gig! Where is the office located?", "role": "B"}, {"speaker": "Emily", "text": "It's right in the arts district. How about you? Are you still playing in that indie rock band?", "role": "A"}, {"speaker": "Michael", "text": "Haha, yes we are! We actually have a gig coming up this Friday night if you're free.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Emily", "text": "Michael! I can't believe we bumped into each other here! It feels like an eternity since our university days.", "role": "A"}, {"speaker": "Michael", "text": "Emily, what a wonderful coincidence! You look incredible! How has life been treating you lately?", "role": "B"}, {"speaker": "Emily", "text": "It's been a whirlwind! I recently transitioned to a lead creative director role at an agency downtown, which keeps me on my toes.", "role": "A"}, {"speaker": "Michael", "text": "That's outstanding! You always had an incredible vision for visual design. How is the team atmosphere?", "role": "B"}, {"speaker": "Emily", "text": "Dynamic and fast-paced! Meanwhile, I heard your band just released a vinyl record—that's monumental!", "role": "A"}, {"speaker": "Michael", "text": "Thanks! It's been a passion project for years. You must join us for our album release party this Friday evening!", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Emily", "text": "Oh my gosh, Michael? Is that you? It's been ages!", "role": "A"}, {"speaker": "Michael", "text": "Emily! Wow, what a surprise! Yes, it's me. How have you been?", "role": "B"}, {"speaker": "Emily", "text": "I've been good! Busy, but good. I actually started a new graphic design job last month.", "role": "A"}, {"speaker": "Michael", "text": "No way, congratulations! That's your dream gig! Where is the office located?", "role": "B"}, {"speaker": "Emily", "text": "It's right in the arts district. How about you? Are you still playing in that indie rock band?", "role": "A"}, {"speaker": "Michael", "text": "Haha, yes we are! We actually have a gig coming up this Friday night if you're free.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "It's been ages (a very long time), Gig (a live musical performance or freelance job)",
            comprehensionQuestion = "What kind of job did Emily recently start?",
            comprehensionOptions = "Software Developer,Graphic Designer,Barista,Music Teacher",
            comprehensionAnswer = "Graphic Designer"
        ),
        ConversationSet(
            id = 4,
            title = "Asking for Airport Directions",
            scenario = "John is at Heathrow Airport looking for the transit trains to central London and asks an airport assistant, Claire.",
            basicDialogueJson = """[{"speaker": "John", "text": "Excuse me, where is the train to the city center?", "role": "A"}, {"speaker": "Claire", "text": "Go straight down this hallway and take the stairs down.", "role": "B"}, {"speaker": "John", "text": "Do I buy a ticket here?", "role": "A"}, {"speaker": "Claire", "text": "Yes, at the ticket machine near the entrance.", "role": "B"}, {"speaker": "John", "text": "Thank you very much!", "role": "A"}, {"speaker": "Claire", "text": "You're welcome! Have a good trip!", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "John", "text": "Excuse me, sorry to bother you, but could you tell me where the express train station is?", "role": "A"}, {"speaker": "Claire", "text": "No bother at all! You'll want to head straight down this corridor, then take the escalators down to Level B1.", "role": "B"}, {"speaker": "John", "text": "Okay, down to B1. Do I need to buy a ticket beforehand, or can I purchase one on the train?", "role": "A"}, {"speaker": "Claire", "text": "You must buy a ticket before boarding. There are ticket kiosks right next to the train gates, or you can tap with a contactless bank card.", "role": "B"}, {"speaker": "John", "text": "Oh, that's perfect! Contactless makes it easy. Thank you so much for your help!", "role": "A"}, {"speaker": "Claire", "text": "You are very welcome. Have a safe and pleasant journey to London!", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "John", "text": "Good morning. Could you kindly direct me to the high-speed rail terminal for downtown transfers?", "role": "A"}, {"speaker": "Claire", "text": "Certainly! Proceed straight through this main concourse, take the high-speed escalators down to concourse level B1, and follow the blue signage.", "role": "B"}, {"speaker": "John", "text": "Perfect. Are international transit passes valid on this line, or should I procure a separate fare ticket at the customer kiosk?", "role": "A"}, {"speaker": "Claire", "text": "Contactless credit card payments and mobile wallet passes are accepted directly at the automated turnstiles for seamless entry.", "role": "B"}, {"speaker": "John", "text": "That saves me a tremendous amount of time before my connecting transfer. I appreciate your thorough guidance!", "role": "A"}, {"speaker": "Claire", "text": "It's my absolute pleasure. Wish you a smooth and effortless transit into the city center!", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "John", "text": "Excuse me, sorry to bother you, but could you tell me where the express train station is?", "role": "A"}, {"speaker": "Claire", "text": "No bother at all! You'll want to head straight down this corridor, then take the escalators down to Level B1.", "role": "B"}, {"speaker": "John", "text": "Okay, down to B1. Do I need to buy a ticket beforehand, or can I purchase one on the train?", "role": "A"}, {"speaker": "Claire", "text": "You must buy a ticket before boarding. There are ticket kiosks right next to the train gates, or you can tap with a contactless bank card.", "role": "B"}, {"speaker": "John", "text": "Oh, that's perfect! Contactless makes it easy. Thank you so much for your help!", "role": "A"}, {"speaker": "Claire", "text": "You are very welcome. Have a safe and pleasant journey to London!", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Corridor (hallway), Escalators (moving staircases), Contactless (paying by tapping cards/phones)",
            comprehensionQuestion = "Where is the express train station located?",
            comprehensionOptions = "Terminal 2,Level B1,Next to baggage claim,Ground level",
            comprehensionAnswer = "Level B1"
        ),
        ConversationSet(
            id = 5,
            title = "At the Doctor's Clinic",
            scenario = "Liam is explaining his seasonal allergy symptoms to Dr. Susan during a routine check-up.",
            basicDialogueJson = """[{"speaker": "Dr. Susan", "text": "Hello Liam. How are you feeling today?", "role": "A"}, {"speaker": "Liam", "text": "Hello Doctor. I have a cold and my throat hurts.", "role": "B"}, {"speaker": "Dr. Susan", "text": "Do you have a fever or a cough?", "role": "A"}, {"speaker": "Liam", "text": "A little cough, but no fever.", "role": "B"}, {"speaker": "Dr. Susan", "text": "Rest well and drink plenty of warm water. Here is a prescription for cough syrup.", "role": "A"}, {"speaker": "Liam", "text": "Thank you Doctor!", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Dr. Susan", "text": "Welcome back, Liam. What seems to be bringing you in today?", "role": "A"}, {"speaker": "Liam", "text": "Well, Doctor, my seasonal allergies have been acting up terribly this year. I can't stop sneezing, and my eyes are always itchy.", "role": "B"}, {"speaker": "Dr. Susan", "text": "I see. Have you been taking any over-the-counter antihistamines?", "role": "A"}, {"speaker": "Liam", "text": "Yes, but they make me feel incredibly drowsy. I can barely focus at work.", "role": "B"}, {"speaker": "Dr. Susan", "text": "That's a common side-effect. Let's try switching you to a non-drowsy prescription spray.", "role": "A"}, {"speaker": "Liam", "text": "That sounds wonderful. I'd love to breathe clearly without falling asleep at my desk!", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Dr. Susan", "text": "Good morning Liam. Let me review your medical history before we discuss your current symptoms. What brings you in today?", "role": "A"}, {"speaker": "Liam", "text": "Doctor, my chronic allergic rhinitis has escalated significantly this spring, causing severe inflammation, sinus pressure, and persistent fatigue.", "role": "B"}, {"speaker": "Dr. Susan", "text": "Have standard second-generation oral antihistamines provided any symptomatic relief, or are you experiencing adverse side effects?", "role": "A"}, {"speaker": "Liam", "text": "Unfortunately, even non-sedating options leave me feeling groggy, which severely impairs my cognitive focus throughout the workday.", "role": "B"}, {"speaker": "Dr. Susan", "text": "Given those parameters, I recommend initiating a combination corticosteroid nasal spray along with targeted immunotherapy evaluations.", "role": "A"}, {"speaker": "Liam", "text": "That sounds like a comprehensive and proactive approach. I'm eager to get this under long-term control.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Dr. Susan", "text": "Welcome back, Liam. What seems to be bringing you in today?", "role": "A"}, {"speaker": "Liam", "text": "Well, Doctor, my seasonal allergies have been acting up terribly this year. I can't stop sneezing, and my eyes are always itchy.", "role": "B"}, {"speaker": "Dr. Susan", "text": "I see. Have you been taking any over-the-counter antihistamines?", "role": "A"}, {"speaker": "Liam", "text": "Yes, but they make me feel incredibly drowsy. I can barely focus at work.", "role": "B"}, {"speaker": "Dr. Susan", "text": "That's a common side-effect. Let's try switching you to a non-drowsy prescription spray.", "role": "A"}, {"speaker": "Liam", "text": "That sounds wonderful. I'd love to breathe clearly without falling asleep at my desk!", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Acting up (experiencing worse symptoms), Antihistamines (allergy medication), Drowsy (feeling sleepy)",
            comprehensionQuestion = "Why did Liam want to switch his allergy medication?",
            comprehensionOptions = "It was too expensive,It made him feel sleepy,It didn't work at all,He ran out of pills",
            comprehensionAnswer = "It made him feel sleepy"
        ),
        ConversationSet(
            id = 6,
            title = "Useful Adjectives",
            scenario = "Practice conversational English for 'Useful Adjectives' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "Please describe it. Is the mountain big or small?", "role": "A"}, {"speaker": "Arun", "text": "It's big. That pebble is small.", "role": "B"}, {"speaker": "Sophia", "text": "Is the bag heavy or light?", "role": "A"}, {"speaker": "Arun", "text": "It's heavy. This one is light.", "role": "B"}, {"speaker": "Sophia", "text": "Is the math difficult or easy?", "role": "A"}, {"speaker": "Arun", "text": "2+2=4 is easy. This is difficult.", "role": "B"}, {"speaker": "Sophia", "text": "Is the car new or old?", "role": "A"}, {"speaker": "Arun", "text": "This is new. That is old.", "role": "B"}, {"speaker": "Sophia", "text": "Is the ring expensive or cheap?", "role": "A"}, {"speaker": "Arun", "text": "It's expensive. This one is cheap.", "role": "B"}, {"speaker": "Sophia", "text": "Is the frame wide or narrow?", "role": "A"}, {"speaker": "Arun", "text": "This is wide. That is narrow.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Priya", "text": "What's your coworker like? Is he boring or funny?", "role": "A"}, {"speaker": "David", "text": "He's actually quite funny, but he gets a little anxious before big meetings.", "role": "B"}, {"speaker": "Priya", "text": "Is he extroverted or introverted?", "role": "A"}, {"speaker": "David", "text": "More extroverted. He's never bad-tempered, and he's always kind and nice.", "role": "B"}, {"speaker": "Priya", "text": "Is he intelligent?", "role": "A"}, {"speaker": "David", "text": "Very intelligent, though he can be indecisive when there are too many choices.", "role": "B"}, {"speaker": "Priya", "text": "What about the taste of the food he brought?", "role": "A"}, {"speaker": "David", "text": "It was a bit bitter and salty, but also strangely sweet. I love things that are spicy or sour, though — he should try that.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sarah", "text": "I don't think anyone would call him boring — he's introverted, sure, but he's genuinely quite funny once you get past his anxious first impression.", "role": "A"}, {"speaker": "Michael", "text": "Right, and unlike our old bad-tempered supervisor, he's remarkably kind, intelligent, and even-tempered under pressure, though admittedly a bit indecisive when the stakes are high.", "role": "B"}, {"speaker": "Sarah", "text": "Speaking of contrasts, that dish had this fascinating umami depth beneath the salty crust — bitter on the first bite, then unexpectedly sweet, with just enough spicy heat and sour brightness to balance it all.", "role": "A"}, {"speaker": "Michael", "text": "It reminds me of that old building downtown — dark, narrow, and a little ugly on the outside, yet strangely beautiful once you're inside. Meanwhile the new high-rise next to it is bright, wide, and expensive to even walk past. I got there early today, for once, instead of running late like usual.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Priya", "text": "What's your coworker like? Is he boring or funny?", "role": "A"}, {"speaker": "David", "text": "He's actually quite funny, but he gets a little anxious before big meetings.", "role": "B"}, {"speaker": "Priya", "text": "Is he extroverted or introverted?", "role": "A"}, {"speaker": "David", "text": "More extroverted. He's never bad-tempered, and he's always kind and nice.", "role": "B"}, {"speaker": "Priya", "text": "Is he intelligent?", "role": "A"}, {"speaker": "David", "text": "Very intelligent, though he can be indecisive when there are too many choices.", "role": "B"}, {"speaker": "Priya", "text": "What about the taste of the food he brought?", "role": "A"}, {"speaker": "David", "text": "It was a bit bitter and salty, but also strangely sweet. I love things that are spicy or sour, though — he should try that.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Useful Adjectives",
            comprehensionQuestion = "What is the main topic discussed in 'Useful Adjectives'?",
            comprehensionOptions = "Core vocabulary for Useful Adjectives,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Useful Adjectives"
        ),
        ConversationSet(
            id = 7,
            title = "Around Town",
            scenario = "Practice conversational English for 'Around Town' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Priya", "text": "Where's the park?", "role": "A"}, {"speaker": "Arun", "text": "It's near here.", "role": "B"}, {"speaker": "Priya", "text": "Where's the church?", "role": "A"}, {"speaker": "Arun", "text": "Next to the park.", "role": "B"}, {"speaker": "Priya", "text": "I want to go to a restaurant.", "role": "A"}, {"speaker": "Arun", "text": "There's one near city hall.", "role": "B"}, {"speaker": "Priya", "text": "Where's the hospital?", "role": "A"}, {"speaker": "Arun", "text": "Near the school.", "role": "B"}, {"speaker": "Priya", "text": "Where's the police station, post office, museum, library, movie theater, and hotel?", "role": "A"}, {"speaker": "Arun", "text": "They're all close together, downtown.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "David", "text": "Excuse me, I want to go to the museum. Do you know where it is?", "role": "A"}, {"speaker": "Emma", "text": "Sure, it's right next to the library, downtown.", "role": "B"}, {"speaker": "David", "text": "And the movie theater?", "role": "A"}, {"speaker": "Emma", "text": "That's a few blocks past the hospital, close to the restaurant district.", "role": "B"}, {"speaker": "David", "text": "What about city hall and the police station?", "role": "A"}, {"speaker": "Emma", "text": "City hall is near the school, and the police station is just past the post office.", "role": "B"}, {"speaker": "David", "text": "One more — where's a good hotel?", "role": "A"}, {"speaker": "Emma", "text": "There's one near the church, not far from the park.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "I'm trying to plan my whole afternoon downtown — I need to stop by city hall, then swing past the post office, grab lunch near a decent restaurant, and still make it to the museum before it closes.", "role": "A"}, {"speaker": "Michael", "text": "If you start at the park, you can walk past the church, then the hospital, and you'll hit the police station and school on the same street before looping back toward the library and movie theater.", "role": "B"}, {"speaker": "Sophia", "text": "That's convenient — is the hotel within walking distance too, in case I want to check in early?", "role": "A"}, {"speaker": "Michael", "text": "It is, actually, right between the library and the museum, so your whole itinerary fits into a fairly compact loop around downtown.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "David", "text": "Excuse me, I want to go to the museum. Do you know where it is?", "role": "A"}, {"speaker": "Emma", "text": "Sure, it's right next to the library, downtown.", "role": "B"}, {"speaker": "David", "text": "And the movie theater?", "role": "A"}, {"speaker": "Emma", "text": "That's a few blocks past the hospital, close to the restaurant district.", "role": "B"}, {"speaker": "David", "text": "What about city hall and the police station?", "role": "A"}, {"speaker": "Emma", "text": "City hall is near the school, and the police station is just past the post office.", "role": "B"}, {"speaker": "David", "text": "One more — where's a good hotel?", "role": "A"}, {"speaker": "Emma", "text": "There's one near the church, not far from the park.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Around Town",
            comprehensionQuestion = "What is the main topic discussed in 'Around Town'?",
            comprehensionOptions = "Core vocabulary for Around Town,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Around Town"
        ),
        ConversationSet(
            id = 8,
            title = "Asking Directions, Buses, Trains & Taxis",
            scenario = "Practice conversational English for 'Asking Directions, Buses, Trains & Taxis' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "David", "text": "How do I get to the station?", "role": "A"}, {"speaker": "Emma", "text": "Go straight, then turn left.", "role": "B"}, {"speaker": "David", "text": "Is it far?", "role": "A"}, {"speaker": "Emma", "text": "No, it's near here.", "role": "B"}, {"speaker": "David", "text": "Does this bus go to downtown?", "role": "A"}, {"speaker": "Driver", "text": "Yes.", "role": "B"}, {"speaker": "David", "text": "What's the next stop?", "role": "A"}, {"speaker": "Driver", "text": "City Hall.", "role": "B"}, {"speaker": "David", "text": "How much is the fare?", "role": "A"}, {"speaker": "Driver", "text": "Two dollars.", "role": "B"}, {"speaker": "David", "text": "Turn right here, please.", "role": "A"}, {"speaker": "Taxi Driver", "text": "Okay.", "role": "B"}, {"speaker": "David", "text": "Here is fine.", "role": "A"}, {"speaker": "Taxi Driver", "text": "Okay.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Michael", "text": "Excuse me, do you know where the train station is?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, go straight for two blocks, then turn left at the corner.", "role": "B"}, {"speaker": "Michael", "text": "Is there a bus stop near here too?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, next to the bank, opposite the pharmacy.", "role": "B"}, {"speaker": "Michael", "text": "How do I get to the subway from there?", "role": "A"}, {"speaker": "Sophia", "text": "Go past the bakery, and it's between the bank and the drugstore.", "role": "B"}, {"speaker": "Michael", "text": "Great. How much is it to the airport by taxi?", "role": "A"}, {"speaker": "Taxi Driver", "text": "About thirty dollars. Turn right here, please, then go straight, please.", "role": "B"}, {"speaker": "Michael", "text": "Can you take me to this place instead? Actually, here is fine, thank you.", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Arun", "text": "Is there a train station near here, or would it be faster to catch the subway from the corner?", "role": "A"}, {"speaker": "Priya", "text": "Honestly, it depends — if you go straight and go past the old bus stop, you'll hit the train station in about ten minutes, but the subway entrance next to it is closer if you don't mind waiting.", "role": "B"}, {"speaker": "Arun", "text": "What's the next stop after that on the subway line, and how much is the fare compared to the bus?", "role": "A"}, {"speaker": "Priya", "text": "The next stop is downtown, and the fare's about the same either way. If you're in a hurry, though, I'd just take a taxi — tell the driver \"turn right here, please\" at the light, then \"go straight, please\" until you reach the hotel.", "role": "B"}, {"speaker": "Arun", "text": "And if I just want to be dropped off early? Can you take me to this place, or should I just say \"here is fine\" once we're close?", "role": "A"}, {"speaker": "Priya", "text": "Either works — most drivers are happy to stop wherever you say, whether that's \"to this hotel, please\" or somewhere along the way.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Michael", "text": "Excuse me, do you know where the train station is?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, go straight for two blocks, then turn left at the corner.", "role": "B"}, {"speaker": "Michael", "text": "Is there a bus stop near here too?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, next to the bank, opposite the pharmacy.", "role": "B"}, {"speaker": "Michael", "text": "How do I get to the subway from there?", "role": "A"}, {"speaker": "Sophia", "text": "Go past the bakery, and it's between the bank and the drugstore.", "role": "B"}, {"speaker": "Michael", "text": "Great. How much is it to the airport by taxi?", "role": "A"}, {"speaker": "Taxi Driver", "text": "About thirty dollars. Turn right here, please, then go straight, please.", "role": "B"}, {"speaker": "Michael", "text": "Can you take me to this place instead? Actually, here is fine, thank you.", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Asking Directions, Buses, Trains & Taxis",
            comprehensionQuestion = "What is the main topic discussed in 'Asking Directions, Buses, Trains & Taxis'?",
            comprehensionOptions = "Core vocabulary for Asking Directions, Buses, Trains & Taxis,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Asking Directions, Buses, Trains & Taxis"
        ),
        ConversationSet(
            id = 9,
            title = "At the Airport",
            scenario = "Practice conversational English for 'At the Airport' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "Where's the baggage claim?", "role": "A"}, {"speaker": "Staff", "text": "It's over there.", "role": "B"}, {"speaker": "Sophia", "text": "Where's the waiting area?", "role": "A"}, {"speaker": "Staff", "text": "Near the metal detector.", "role": "B"}, {"speaker": "Sophia", "text": "Can you help me find my suitcase?", "role": "A"}, {"speaker": "Staff", "text": "Yes, follow me.", "role": "B"}, {"speaker": "Sophia", "text": "Where's the gate?", "role": "A"}, {"speaker": "Staff", "text": "Gate 12, past the kiosk.", "role": "B"}, {"speaker": "Sophia", "text": "Where's self check-in?", "role": "A"}, {"speaker": "Staff", "text": "Near the check-in counter.", "role": "B"}, {"speaker": "Sophia", "text": "Where's the departures board?", "role": "A"}, {"speaker": "Staff", "text": "By the escalator.", "role": "B"}, {"speaker": "Sophia", "text": "Where's the arrivals board?", "role": "A"}, {"speaker": "Staff", "text": "On your right.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "David", "text": "Is this an international flight or a domestic flight?", "role": "A"}, {"speaker": "Emma", "text": "International. We need to go through customs at the departure gate.", "role": "B"}, {"speaker": "David", "text": "Do you have your passport and visa ready?", "role": "A"}, {"speaker": "Emma", "text": "Yes, and my luggage is already checked.", "role": "B"}, {"speaker": "Officer", "text": "Your passport, please.", "role": "B"}, {"speaker": "Emma", "text": "Here's my passport.", "role": "B"}, {"speaker": "Officer", "text": "What's the purpose of this trip?", "role": "B"}, {"speaker": "Emma", "text": "Sightseeing.", "role": "B"}, {"speaker": "Officer", "text": "How long will you be staying?", "role": "B"}, {"speaker": "Emma", "text": "About two weeks.", "role": "B"}, {"speaker": "Officer", "text": "What's inside your bag?", "role": "B"}, {"speaker": "Emma", "text": "Only my personal belongings — these are presents from my friend.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Michael", "text": "Since it's an international flight, we'll need to clear customs before boarding, so make sure your passport and visa are both accessible, not buried in your suitcase.", "role": "A"}, {"speaker": "Sophia", "text": "Already sorted — I checked my luggage at the counter, and I'm just deciding between the self check-in kiosk or the regular check-in counter, since the departures board says our gate desk hasn't opened yet.", "role": "B"}, {"speaker": "Officer", "text": "Your passport, please. What's the purpose of this trip — is it business or sightseeing?", "role": "B"}, {"speaker": "Michael", "text": "Sightseeing, mostly. How long will you be staying, you might ask — about two weeks, and everything inside my bag is just personal belongings, along with a few presents from my friend that I'm bringing back.", "role": "A"}, {"speaker": "Sophia", "text": "While we wait, I might grab a keychain, a mug, or a T-shirt at the kiosk near the waiting area — maybe a magnet or postcard too, and I'll skip the alcohol this time since it's a hassle through the metal detector line.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "David", "text": "Is this an international flight or a domestic flight?", "role": "A"}, {"speaker": "Emma", "text": "International. We need to go through customs at the departure gate.", "role": "B"}, {"speaker": "David", "text": "Do you have your passport and visa ready?", "role": "A"}, {"speaker": "Emma", "text": "Yes, and my luggage is already checked.", "role": "B"}, {"speaker": "Officer", "text": "Your passport, please.", "role": "B"}, {"speaker": "Emma", "text": "Here's my passport.", "role": "B"}, {"speaker": "Officer", "text": "What's the purpose of this trip?", "role": "B"}, {"speaker": "Emma", "text": "Sightseeing.", "role": "B"}, {"speaker": "Officer", "text": "How long will you be staying?", "role": "B"}, {"speaker": "Emma", "text": "About two weeks.", "role": "B"}, {"speaker": "Officer", "text": "What's inside your bag?", "role": "B"}, {"speaker": "Emma", "text": "Only my personal belongings — these are presents from my friend.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for At the Airport",
            comprehensionQuestion = "What is the main topic discussed in 'At the Airport'?",
            comprehensionOptions = "Core vocabulary for At the Airport,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for At the Airport"
        ),
        ConversationSet(
            id = 10,
            title = "Business English",
            scenario = "Practice conversational English for 'Business English' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Priya", "text": "What is in your office?", "role": "A"}, {"speaker": "Arun", "text": "There is a refrigerator, a desk, and a chair.", "role": "B"}, {"speaker": "Priya", "text": "Is there a whiteboard?", "role": "A"}, {"speaker": "Arun", "text": "Yes, and a telephone and a stapler.", "role": "B"}, {"speaker": "Priya", "text": "Is there a sofa?", "role": "A"}, {"speaker": "Arun", "text": "Yes, and a keyboard and a monitor.", "role": "B"}, {"speaker": "Priya", "text": "Is there a copier?", "role": "A"}, {"speaker": "Arun", "text": "Yes, and a coffee maker and a microwave.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Manager", "text": "Good morning, everyone. We're here today to discuss the new project. Let's begin, shall we?", "role": "A"}, {"speaker": "David", "text": "I'd like to introduce our new client, Mr. Chen, the general manager.", "role": "B"}, {"speaker": "Employee", "text": "What are your views on this proposal?", "role": "B"}, {"speaker": "Manager", "text": "The way I see it, we should start with the budget.", "role": "A"}, {"speaker": "Employee 2", "text": "I don't really agree — I think we should start with the timeline.", "role": "B"}, {"speaker": "Manager", "text": "I didn't catch that. Could you repeat that, please?", "role": "A"}, {"speaker": "Employee 2", "text": "Precisely, the timeline should come first.", "role": "B"}, {"speaker": "Manager", "text": "I get your point. Thank you all for coming.", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Manager", "text": "So, let's start with the numbers — I know some of you don't really agree with the projected budget, so let's hear it. What are your views on this?", "role": "A"}, {"speaker": "Sarah", "text": "The way I see it, before we commit to any figures, we should bring in the section manager and the assistant general manager, since they'll be introducing this to the client directly — this is the section manager, Mr. Kim, by the way, and this is the assistant manager, Ms. Rao.", "role": "B"}, {"speaker": "Manager", "text": "Fair point. I'd like to introduce everyone properly, then: this is our President, our executive managing director, our managing director, and our chief, all joining remotely for this portion.", "role": "A"}, {"speaker": "Sarah", "text": "I didn't quite catch what the chief said earlier — could you repeat that, please? It sounded important.", "role": "B"}, {"speaker": "Manager", "text": "Precisely what I was thinking. Well, thank you all for coming — remember, embrace the pain to inherit the gain, and if you want something done right, do it yourself, but also, give assistance, not advice, in a crisis. No rest for the weary this quarter, unfortunately — it really is another day, another dollar, but let's make hay while the sun shines.", "role": "A"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Manager", "text": "Good morning, everyone. We're here today to discuss the new project. Let's begin, shall we?", "role": "A"}, {"speaker": "David", "text": "I'd like to introduce our new client, Mr. Chen, the general manager.", "role": "B"}, {"speaker": "Employee", "text": "What are your views on this proposal?", "role": "B"}, {"speaker": "Manager", "text": "The way I see it, we should start with the budget.", "role": "A"}, {"speaker": "Employee 2", "text": "I don't really agree — I think we should start with the timeline.", "role": "B"}, {"speaker": "Manager", "text": "I didn't catch that. Could you repeat that, please?", "role": "A"}, {"speaker": "Employee 2", "text": "Precisely, the timeline should come first.", "role": "B"}, {"speaker": "Manager", "text": "I get your point. Thank you all for coming.", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Business English",
            comprehensionQuestion = "What is the main topic discussed in 'Business English'?",
            comprehensionOptions = "Core vocabulary for Business English,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Business English"
        ),
        ConversationSet(
            id = 11,
            title = "Shopping for Clothes",
            scenario = "Practice conversational English for 'Shopping for Clothes' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Clerk", "text": "How may I help you?", "role": "A"}, {"speaker": "Sophia", "text": "I'm looking for a sweater.", "role": "B"}, {"speaker": "Clerk", "text": "What about a shirt or a T-shirt?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, and pants and shorts.", "role": "B"}, {"speaker": "Clerk", "text": "Do you need underwear or a jacket?", "role": "A"}, {"speaker": "Sophia", "text": "A jacket, please. Also a suit, a skirt, a dress, a blouse, and a hat.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "David", "text": "Can I try it on?", "role": "A"}, {"speaker": "Clerk", "text": "Of course. The fitting room is right there.", "role": "B"}, {"speaker": "David", "text": "Do you have this in a bigger size, or a different color?", "role": "A"}, {"speaker": "Clerk", "text": "Let me check. Do you take credit cards, you're wondering? Yes, we do.", "role": "B"}, {"speaker": "David", "text": "Can I exchange it if it doesn't fit?", "role": "A"}, {"speaker": "Clerk", "text": "Yes, absolutely. Where is the fitting room, again — over there, past the mirrors.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Priya", "text": "I love to go shopping, but I hardly ever sell my old clothes — I mostly just look for pieces that match what I already have, then return the ones that don't work out once I get home.", "role": "A"}, {"speaker": "Arun", "text": "Do you have a color you gravitate toward? I usually go for white, yellow, or green, though I'll wear gray, navy, blue, or black if the occasion calls for it.", "role": "B"}, {"speaker": "Priya", "text": "This red shirt looked promising, but it's far too tight and the sleeves are too short — I need something looser and longer, not too big and not too small, which is surprisingly hard to find.", "role": "A"}, {"speaker": "Arun", "text": "Have you asked if they have it in a bigger size, or checked whether they'll let you exchange it for a different color once you've had a chance to try it on properly in the fitting room?", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "David", "text": "Can I try it on?", "role": "A"}, {"speaker": "Clerk", "text": "Of course. The fitting room is right there.", "role": "B"}, {"speaker": "David", "text": "Do you have this in a bigger size, or a different color?", "role": "A"}, {"speaker": "Clerk", "text": "Let me check. Do you take credit cards, you're wondering? Yes, we do.", "role": "B"}, {"speaker": "David", "text": "Can I exchange it if it doesn't fit?", "role": "A"}, {"speaker": "Clerk", "text": "Yes, absolutely. Where is the fitting room, again — over there, past the mirrors.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Shopping for Clothes",
            comprehensionQuestion = "What is the main topic discussed in 'Shopping for Clothes'?",
            comprehensionOptions = "Core vocabulary for Shopping for Clothes,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Shopping for Clothes"
        ),
        ConversationSet(
            id = 12,
            title = "Making Complaints",
            scenario = "Practice conversational English for 'Making Complaints' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Noriko", "text": "What's the matter?", "role": "A"}, {"speaker": "Mark", "text": "The office is hot.", "role": "B"}, {"speaker": "Noriko", "text": "Is it ever cold?", "role": "A"}, {"speaker": "Mark", "text": "Yes, sometimes cold.", "role": "B"}, {"speaker": "Noriko", "text": "Is it noisy?", "role": "A"}, {"speaker": "Mark", "text": "Yes, and dark and dirty too.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Sarah", "text": "My coworker always causes a delay, and honestly, it's a bit rude.", "role": "A"}, {"speaker": "Michael", "text": "Does he ever complain himself, or just make mistakes?", "role": "B"}, {"speaker": "Sarah", "text": "He makes mistakes, and when I mention it, he says I'm being inconvenient, which really annoys me.", "role": "A"}, {"speaker": "Michael", "text": "Did he insult you directly?", "role": "B"}, {"speaker": "Sarah", "text": "No, but he did yell, and it felt like he was trying to reject my feedback and irritate me on purpose — it caused a real problem for the team.", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Emma", "text": "Can I have a refund? It's too small, and honestly, the change was wrong when I paid, and I ended up with the wrong color anyway.", "role": "A"}, {"speaker": "Clerk", "text": "I'm terribly sorry about that — let me look into both issues right away.", "role": "B"}, {"speaker": "Emma", "text": "On top of that, it has a stain here, there's a hole here, and if I'm being completely honest, it even has a crack here. Can I exchange it for a new one instead of a refund?", "role": "A"}, {"speaker": "Clerk", "text": "Absolutely, and I apologize again — issues like a stain, a hole, or a crack should never make it past our quality check, so let's get this sorted immediately.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Sarah", "text": "My coworker always causes a delay, and honestly, it's a bit rude.", "role": "A"}, {"speaker": "Michael", "text": "Does he ever complain himself, or just make mistakes?", "role": "B"}, {"speaker": "Sarah", "text": "He makes mistakes, and when I mention it, he says I'm being inconvenient, which really annoys me.", "role": "A"}, {"speaker": "Michael", "text": "Did he insult you directly?", "role": "B"}, {"speaker": "Sarah", "text": "No, but he did yell, and it felt like he was trying to reject my feedback and irritate me on purpose — it caused a real problem for the team.", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Making Complaints",
            comprehensionQuestion = "What is the main topic discussed in 'Making Complaints'?",
            comprehensionOptions = "Core vocabulary for Making Complaints,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Making Complaints"
        ),
        ConversationSet(
            id = 13,
            title = "Dining Like a Champ",
            scenario = "Practice conversational English for 'Dining Like a Champ' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "David", "text": "I have a reservation at nine.", "role": "A"}, {"speaker": "Host", "text": "Do you have a table for two?", "role": "B"}, {"speaker": "David", "text": "Yes. May I have a menu?", "role": "A"}, {"speaker": "Host", "text": "Here you go.", "role": "B"}, {"speaker": "David", "text": "May I order? I'll have this, please.", "role": "A"}, {"speaker": "Host", "text": "Sure.", "role": "B"}, {"speaker": "David", "text": "Excuse me, my order hasn't come yet.", "role": "A"}, {"speaker": "Host", "text": "Sorry, checking now.", "role": "B"}, {"speaker": "David", "text": "Check, please.", "role": "A"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Waiter", "text": "Point and speak — just say \"~ please.\" What are today's specials?", "role": "A"}, {"speaker": "Sophia", "text": "We have chicken, beef, pork, salad, and seafood today.", "role": "B"}, {"speaker": "Waiter", "text": "Would you like an appetizer, main dish, or dessert first?", "role": "A"}, {"speaker": "Sophia", "text": "Main dish, please, and can I have a drink menu?", "role": "B"}, {"speaker": "Waiter", "text": "Of course. Anything else — more napkins, perhaps?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, please. And counting — one, two, three, four, five of us tonight.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Priya", "text": "With meat, please, though without bell pepper — and could you check if this dish contains any peanuts, since I can't eat or drink alcohol either, so please remove the wine from the sauce if it's used.", "role": "A"}, {"speaker": "Waiter", "text": "Understood — I'll also make sure there's no onion, cheese, or tomato, given your preferences, and I'll bring vegetables, butter, sugar, and olive oil on the side instead.", "role": "B"}, {"speaker": "Priya", "text": "Perfect, and could you bring a spoon, a fork, a knife, and a napkin, along with some salt and black pepper? I'll also need water, bread, and coffee once the main dish arrives.", "role": "A"}, {"speaker": "Waiter", "text": "Certainly. And for what it's worth, it's delicious, it looks tasty, and it's very good tonight — though I'll admit yesterday's batch was slightly overcooked and lacked salt; today's smells so nice and isn't the least bit raw or too spicy.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Waiter", "text": "Point and speak — just say \"~ please.\" What are today's specials?", "role": "A"}, {"speaker": "Sophia", "text": "We have chicken, beef, pork, salad, and seafood today.", "role": "B"}, {"speaker": "Waiter", "text": "Would you like an appetizer, main dish, or dessert first?", "role": "A"}, {"speaker": "Sophia", "text": "Main dish, please, and can I have a drink menu?", "role": "B"}, {"speaker": "Waiter", "text": "Of course. Anything else — more napkins, perhaps?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, please. And counting — one, two, three, four, five of us tonight.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Dining Like a Champ",
            comprehensionQuestion = "What is the main topic discussed in 'Dining Like a Champ'?",
            comprehensionOptions = "Core vocabulary for Dining Like a Champ,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Dining Like a Champ"
        ),
        ConversationSet(
            id = 14,
            title = "Food Preferences, Allergies & Restaurant Vocabulary",
            scenario = "Practice conversational English for 'Food Preferences, Allergies & Restaurant Vocabulary' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "I am allergic to shellfish.", "role": "A"}, {"speaker": "David", "text": "I am a vegetarian.", "role": "B"}, {"speaker": "Sophia", "text": "I am allergic to peanuts too.", "role": "A"}, {"speaker": "David", "text": "I can't eat pork. My wife is a vegan.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Emma", "text": "It's delicious! It looks tasty, and it's very good.", "role": "A"}, {"speaker": "Michael", "text": "Really? Mine is overcooked, and it lacks salt.", "role": "B"}, {"speaker": "Emma", "text": "That's a shame — mine smells so nice.", "role": "A"}, {"speaker": "Michael", "text": "This is not fresh, and it's too spicy for me. Can you bring me a fork and a napkin, please? I need a spoon too.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Arun", "text": "Does this dish contain any shellfish, eggs, milk, wheat, or soy? I'm allergic to meat as well, oddly enough, and I can't eat or drink alcohol in any form.", "role": "A"}, {"speaker": "Waiter", "text": "Let me double-check with the kitchen — we can absolutely remove ~ from this dish if needed, and prepare it without butter, cheese, or tomato as well.", "role": "B"}, {"speaker": "Arun", "text": "I'd appreciate that. My friend here is a vegan, so please leave out fish entirely, and I need a knife, black pepper, and dessert menu once we're ready.", "role": "A"}, {"speaker": "Waiter", "text": "Of course — and just to confirm, it's overcooked or raw complaints aside, tonight's dish smells so nice and isn't remotely too spicy, so I think you'll both be pleased.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Emma", "text": "It's delicious! It looks tasty, and it's very good.", "role": "A"}, {"speaker": "Michael", "text": "Really? Mine is overcooked, and it lacks salt.", "role": "B"}, {"speaker": "Emma", "text": "That's a shame — mine smells so nice.", "role": "A"}, {"speaker": "Michael", "text": "This is not fresh, and it's too spicy for me. Can you bring me a fork and a napkin, please? I need a spoon too.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Food Preferences, Allergies & Restaurant Vocabulary",
            comprehensionQuestion = "What is the main topic discussed in 'Food Preferences, Allergies & Restaurant Vocabulary'?",
            comprehensionOptions = "Core vocabulary for Food Preferences, Allergies & Restaurant Vocabulary,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Food Preferences, Allergies & Restaurant Vocabulary"
        ),
        ConversationSet(
            id = 15,
            title = "Emergency Words and Phrases",
            scenario = "Practice conversational English for 'Emergency Words and Phrases' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Operator", "text": "This is 911. Please state the nature of your emergency.", "role": "A"}, {"speaker": "Caller", "text": "I need a doctor! There is a fire!", "role": "B"}, {"speaker": "Doctor", "text": "What's wrong?", "role": "B"}, {"speaker": "Patient", "text": "My head hurts.", "role": "B"}, {"speaker": "Patient", "text": "My stomach, tooth, knee, chest, back, ankle, and ear hurt too.", "role": "B"}, {"speaker": "Patient", "text": "I'm suffering from asthma.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Doctor", "text": "What's wrong? Does your stomach or your tooth hurt?", "role": "A"}, {"speaker": "Patient", "text": "My knee hurts, and my chest feels tight.", "role": "B"}, {"speaker": "Doctor", "text": "Any conditions — diabetes, an allergy, or heart disease?", "role": "A"}, {"speaker": "Patient", "text": "I'm suffering from asthma, and I take medicine and antibiotics regularly.", "role": "B"}, {"speaker": "Doctor", "text": "Have you had your vaccine, and do you have a prescription with you?", "role": "A"}, {"speaker": "Patient", "text": "Yes, though the doctor said it might be a virus or bacteria this time.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Officer", "text": "In case of an emergency, when in the United States, dial 911 — this is 911, please state the nature of your emergency.", "role": "A"}, {"speaker": "Tourist", "text": "There was an accident! I was robbed, and I want to report a crime — I'm being harassed and honestly, I am lost, so please give me directions to the nearest embassy.", "role": "B"}, {"speaker": "Officer", "text": "I understand. I'm Officer Reyes, my location is downtown precinct three, and my phone number is on file — could you tell me your name, and would you like to leave a message with my badge number for reference?", "role": "A"}, {"speaker": "Tourist", "text": "I found this item earlier too, and someone's gone missing near the hotel — I lost my passport, wallet, and phone, and someone stole my camera, suitcase, and money, all in the same afternoon, on top of worrying about the coming storm, hurricane, and flood warnings on the news. By the way, where's the restroom — I need toilet paper and I need Wi-Fi to contact my family.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Doctor", "text": "What's wrong? Does your stomach or your tooth hurt?", "role": "A"}, {"speaker": "Patient", "text": "My knee hurts, and my chest feels tight.", "role": "B"}, {"speaker": "Doctor", "text": "Any conditions — diabetes, an allergy, or heart disease?", "role": "A"}, {"speaker": "Patient", "text": "I'm suffering from asthma, and I take medicine and antibiotics regularly.", "role": "B"}, {"speaker": "Doctor", "text": "Have you had your vaccine, and do you have a prescription with you?", "role": "A"}, {"speaker": "Patient", "text": "Yes, though the doctor said it might be a virus or bacteria this time.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Emergency Words and Phrases",
            comprehensionQuestion = "What is the main topic discussed in 'Emergency Words and Phrases'?",
            comprehensionOptions = "Core vocabulary for Emergency Words and Phrases,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Emergency Words and Phrases"
        ),
        ConversationSet(
            id = 16,
            title = "Family & Relatives",
            scenario = "Practice conversational English for 'Family & Relatives' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Michael", "text": "What kind of person is your grandmother?", "role": "A"}, {"speaker": "Sarah", "text": "She's kind. My grandfather is funny.", "role": "B"}, {"speaker": "Michael", "text": "How's your mother?", "role": "A"}, {"speaker": "Sarah", "text": "My mother is well.", "role": "B"}, {"speaker": "Michael", "text": "Do you have a sister?", "role": "A"}, {"speaker": "Sarah", "text": "A younger sister and an older sister.", "role": "B"}, {"speaker": "Michael", "text": "A brother?", "role": "A"}, {"speaker": "Sarah", "text": "An older brother and a younger brother.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Priya", "text": "Tell me about your family — your uncle, cousin, and aunt?", "role": "A"}, {"speaker": "Arun", "text": "My uncle and cousin live with my aunt nearby. My father and mother are both teachers.", "role": "B"}, {"speaker": "Priya", "text": "Forward my greetings to your husband and wife!", "role": "A"}, {"speaker": "Arun", "text": "I will, and my son and daughter say hello too.", "role": "B"}, {"speaker": "Priya", "text": "Do you own any pets?", "role": "A"}, {"speaker": "Arun", "text": "I own a dog and a cat, plus a bird, a fish, a bunny, and even a snake.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "What kind of person is your grandfather, and how's your grandmother doing these days?", "role": "A"}, {"speaker": "David", "text": "My grandmother is well, thank you — she's kind, and my grandfather remains as funny as ever, even at his age. My uncle, cousin, and aunt all still live together, actually, which makes holidays chaotic but wonderful.", "role": "B"}, {"speaker": "Sophia", "text": "Forward my greetings to your husband and to your wife's side of the family too, if you don't mind — and to your son and daughter as well.", "role": "A"}, {"speaker": "David", "text": "I will, thank you. Between my younger sister, older sister, older brother, and younger brother, our house is already full — and that's before counting the pets. I own a dog, a cat, a bird, a fish, a bunny, and, believe it or not, a snake too.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Priya", "text": "Tell me about your family — your uncle, cousin, and aunt?", "role": "A"}, {"speaker": "Arun", "text": "My uncle and cousin live with my aunt nearby. My father and mother are both teachers.", "role": "B"}, {"speaker": "Priya", "text": "Forward my greetings to your husband and wife!", "role": "A"}, {"speaker": "Arun", "text": "I will, and my son and daughter say hello too.", "role": "B"}, {"speaker": "Priya", "text": "Do you own any pets?", "role": "A"}, {"speaker": "Arun", "text": "I own a dog and a cat, plus a bird, a fish, a bunny, and even a snake.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Family & Relatives",
            comprehensionQuestion = "What is the main topic discussed in 'Family & Relatives'?",
            comprehensionOptions = "Core vocabulary for Family & Relatives,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Family & Relatives"
        ),
        ConversationSet(
            id = 17,
            title = "Your Feelings",
            scenario = "Practice conversational English for 'Your Feelings' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "David", "text": "How are you doing?", "role": "A"}, {"speaker": "Sophia", "text": "I'm happy.", "role": "B"}, {"speaker": "David", "text": "Are you tired?", "role": "A"}, {"speaker": "Sophia", "text": "I'm not tired. I'm anxious.", "role": "B"}, {"speaker": "David", "text": "Were you bored yesterday?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, and a little sad.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Priya", "text": "How are you doing today?", "role": "A"}, {"speaker": "Arun", "text": "I'm angry, honestly, and a bit anxious about the exam.", "role": "B"}, {"speaker": "Priya", "text": "Were you embarrassed at the meeting yesterday?", "role": "A"}, {"speaker": "Arun", "text": "A little, but I feel great today — active, calm, and hopeful.", "role": "B"}, {"speaker": "Priya", "text": "Not disgusted or horrified anymore?", "role": "A"}, {"speaker": "Arun", "text": "No, I'm actually proud, relaxed, and satisfied with how it turned out.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sarah", "text": "You look tired.", "role": "A"}, {"speaker": "John", "text": "I'm not tired — I'm anxious today, honestly, and a little disgusted by the news this morning, though I was in love with the project by lunchtime, if that makes sense.", "role": "B"}, {"speaker": "Sarah", "text": "I know the feeling. I'm annoyed and confused about the schedule myself, and frankly exhausted, frightened, and frustrated by how miserable this week has been — nervous doesn't even cover it.", "role": "A"}, {"speaker": "John", "text": "It sounds silly, but I feel terrible, upset, and worried too, even though I'm super happy about the weekend and excited for tomorrow. I'm angry at him for canceling, annoyed by what's happened, and I was embarrassed yesterday — yet somehow I'm feeling great today, even if I'm still a little frustrated and lonely underneath it all.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Priya", "text": "How are you doing today?", "role": "A"}, {"speaker": "Arun", "text": "I'm angry, honestly, and a bit anxious about the exam.", "role": "B"}, {"speaker": "Priya", "text": "Were you embarrassed at the meeting yesterday?", "role": "A"}, {"speaker": "Arun", "text": "A little, but I feel great today — active, calm, and hopeful.", "role": "B"}, {"speaker": "Priya", "text": "Not disgusted or horrified anymore?", "role": "A"}, {"speaker": "Arun", "text": "No, I'm actually proud, relaxed, and satisfied with how it turned out.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Your Feelings",
            comprehensionQuestion = "What is the main topic discussed in 'Your Feelings'?",
            comprehensionOptions = "Core vocabulary for Your Feelings,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Your Feelings"
        ),
        ConversationSet(
            id = 18,
            title = "Geography in English",
            scenario = "Practice conversational English for 'Geography in English' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Teacher", "text": "Can you find South America?", "role": "A"}, {"speaker": "Student", "text": "Yes, here.", "role": "B"}, {"speaker": "Teacher", "text": "What continent is this?", "role": "A"}, {"speaker": "Student", "text": "This is Africa.", "role": "B"}, {"speaker": "Teacher", "text": "And this?", "role": "A"}, {"speaker": "Student", "text": "It's Asia.", "role": "B"}, {"speaker": "Teacher", "text": "What's north?", "role": "A"}, {"speaker": "Student", "text": "North America.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Arun", "text": "Which way is north, south, east, and west from here?", "role": "A"}, {"speaker": "Priya", "text": "North is that way, south behind us, east to the right, west to the left.", "role": "B"}, {"speaker": "Arun", "text": "Is the store in front or in back, inside or outside?", "role": "A"}, {"speaker": "Priya", "text": "In front, inside — it's above the garage, not under it.", "role": "B"}, {"speaker": "Arun", "text": "Where do you live, and how many countries have you visited?", "role": "A"}, {"speaker": "Priya", "text": "I currently live in Boston, and I've visited ten countries. I really want to visit Japan.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "David", "text": "Can you find the continents on the map — this is South America, next to Central America, with the Atlantic Ocean separating it from Africa and Europe?", "role": "A"}, {"speaker": "Emma", "text": "And up north, that's North America, bordered by the Arctic Ocean, while Asia stretches across from Europe, wrapped by the Pacific Ocean on one side and the Indian Ocean near Oceania and Antarctica on the other.", "role": "B"}, {"speaker": "David", "text": "I currently live near a coastal city with a beautiful ocean view, a nearby mountain range, and a river that flows past a lake, so I've grown attached to landscapes with a sky full of islands offshore.", "role": "A"}, {"speaker": "Emma", "text": "I prefer inland scenery myself — a hill, some mainland forest, a quiet pond, exposed rock, seaside fields, and even the desert, where the soil, dirt, and grass all tell a different story. How many countries have you visited, by the way, and where do you want to visit next — I hear Tokyo, New York, London, New Delhi, Beijing, and Sydney are all worth the trip.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Arun", "text": "Which way is north, south, east, and west from here?", "role": "A"}, {"speaker": "Priya", "text": "North is that way, south behind us, east to the right, west to the left.", "role": "B"}, {"speaker": "Arun", "text": "Is the store in front or in back, inside or outside?", "role": "A"}, {"speaker": "Priya", "text": "In front, inside — it's above the garage, not under it.", "role": "B"}, {"speaker": "Arun", "text": "Where do you live, and how many countries have you visited?", "role": "A"}, {"speaker": "Priya", "text": "I currently live in Boston, and I've visited ten countries. I really want to visit Japan.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Geography in English",
            comprehensionQuestion = "What is the main topic discussed in 'Geography in English'?",
            comprehensionOptions = "Core vocabulary for Geography in English,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Geography in English"
        ),
        ConversationSet(
            id = 19,
            title = "Checking in a Hotel",
            scenario = "Practice conversational English for 'Checking in a Hotel' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Guest", "text": "Is there a TV in this hotel?", "role": "A"}, {"speaker": "Staff", "text": "Yes, there's a TV.", "role": "B"}, {"speaker": "Guest", "text": "Is there an elevator?", "role": "A"}, {"speaker": "Staff", "text": "Yes.", "role": "B"}, {"speaker": "Guest", "text": "Is there a shower?", "role": "A"}, {"speaker": "Staff", "text": "Yes, and a bathtub and a sink.", "role": "B"}, {"speaker": "Guest", "text": "I'd like to check in.", "role": "A"}, {"speaker": "Clerk", "text": "Sure. Here's your key.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Guest", "text": "I'd like to check in. Is there a room service cart available, and does the room have a double-sized bed?", "role": "A"}, {"speaker": "Clerk", "text": "Yes, and there's a twin-sized bed option too, plus a vending machine down the corridor near the elevator.", "role": "B"}, {"speaker": "Guest", "text": "What time is check out, and is breakfast included?", "role": "A"}, {"speaker": "Clerk", "text": "Checkout is at noon, and yes, breakfast is included. Could you please recommend a good restaurant around here, you might ask — I'd suggest the one near the front desk.", "role": "B"}, {"speaker": "Guest", "text": "Could you please call me a taxi, and do you have a city guide?", "role": "A"}, {"speaker": "Clerk", "text": "Of course, right here.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Guest", "text": "Are you staying at a hotel, a resort, or did you book a bed & breakfast this time? Personally, I prefer an inn over a full resort, though my brother swears by hostels, and we're even considering a campground for part of the trip.", "role": "A"}, {"speaker": "David", "text": "I'd like to check in — does the room have cable TV, air conditioning, a heater, soap, and shampoo, along with a toothbrush and reliable Wi-Fi?", "role": "B"}, {"speaker": "Clerk", "text": "All included, and we also have a swimming pool, a gym, and rooms with either a mountain view or an ocean view — everything's all inclusive, and here's your room number and key.", "role": "B"}, {"speaker": "David", "text": "Unfortunately, I have a complaint — the Wi-Fi is not working, the bathwater is cold, there're no towels, and it's too noisy on this floor.", "role": "B"}, {"speaker": "Clerk", "text": "I sincerely apologize for all of that — let me move you to a quieter room immediately and have someone check the room service cart, king-sized bed suite, and bathroom fixtures right away.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Guest", "text": "I'd like to check in. Is there a room service cart available, and does the room have a double-sized bed?", "role": "A"}, {"speaker": "Clerk", "text": "Yes, and there's a twin-sized bed option too, plus a vending machine down the corridor near the elevator.", "role": "B"}, {"speaker": "Guest", "text": "What time is check out, and is breakfast included?", "role": "A"}, {"speaker": "Clerk", "text": "Checkout is at noon, and yes, breakfast is included. Could you please recommend a good restaurant around here, you might ask — I'd suggest the one near the front desk.", "role": "B"}, {"speaker": "Guest", "text": "Could you please call me a taxi, and do you have a city guide?", "role": "A"}, {"speaker": "Clerk", "text": "Of course, right here.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Checking in a Hotel",
            comprehensionQuestion = "What is the main topic discussed in 'Checking in a Hotel'?",
            comprehensionOptions = "Core vocabulary for Checking in a Hotel,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Checking in a Hotel"
        ),
        ConversationSet(
            id = 20,
            title = "Crush Your Goals — Study Strategy",
            scenario = "Practice conversational English for 'Crush Your Goals — Study Strategy' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Teacher", "text": "Why are you learning English?", "role": "A"}, {"speaker": "Student 1", "text": "I want to travel to the United States.", "role": "B"}, {"speaker": "Student 2", "text": "It's a beautiful language.", "role": "B"}, {"speaker": "Student 3", "text": "It's useful for my job.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Teacher", "text": "Why are you learning English?", "role": "A"}, {"speaker": "Student 1", "text": "I love American culture and people, and it's part of my university studies.", "role": "B"}, {"speaker": "Student 2", "text": "I want to speak to my partner's family in English.", "role": "B"}, {"speaker": "Student 3", "text": "I live in the United States now, so it's necessary.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Teacher", "text": "Why are you learning English, really — beyond the obvious reasons?", "role": "A"}, {"speaker": "Student 1", "text": "Honestly, it started because I wanted to travel to the United States, but along the way I realized it's simply a beautiful language, and it's become useful for my job in ways I didn't expect.", "role": "B"}, {"speaker": "Student 2", "text": "For me, it's more personal — I love American culture and its people, and since I want to speak to my partner's family in English, the motivation runs deeper than any classroom requirement.", "role": "B"}, {"speaker": "Student 3", "text": "I live in the United States now, so it stopped being optional, but I also genuinely love learning languages in general — it was part of my university studies originally, and that curiosity never really left.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Teacher", "text": "Why are you learning English?", "role": "A"}, {"speaker": "Student 1", "text": "I love American culture and people, and it's part of my university studies.", "role": "B"}, {"speaker": "Student 2", "text": "I want to speak to my partner's family in English.", "role": "B"}, {"speaker": "Student 3", "text": "I live in the United States now, so it's necessary.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Crush Your Goals — Study Strategy",
            comprehensionQuestion = "What is the main topic discussed in 'Crush Your Goals — Study Strategy'?",
            comprehensionOptions = "Core vocabulary for Crush Your Goals — Study Strategy,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Crush Your Goals — Study Strategy"
        ),
        ConversationSet(
            id = 21,
            title = "Question Words, Sentence Patterns & Grammar",
            scenario = "Practice conversational English for 'Question Words, Sentence Patterns & Grammar' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Teacher", "text": "Why did you say that?", "role": "A"}, {"speaker": "Student", "text": "I don't know why.", "role": "B"}, {"speaker": "Teacher", "text": "My name is Adam.", "role": "A"}, {"speaker": "Student", "text": "My name is Maria.", "role": "B"}, {"speaker": "Teacher", "text": "What time is it? It's 9 o'clock.", "role": "A"}, {"speaker": "Student", "text": "I like music.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Priya", "text": "Why did you say that, and when did this happen?", "role": "A"}, {"speaker": "Arun", "text": "I don't know why, and it happened yesterday, actually.", "role": "B"}, {"speaker": "Priya", "text": "Where did it happen, and who told you?", "role": "A"}, {"speaker": "Arun", "text": "At the office, and my coworker told me.", "role": "B"}, {"speaker": "Priya", "text": "How did you find out, and how much did it cost?", "role": "A"}, {"speaker": "Arun", "text": "He mentioned it casually. It cost quite a bit, honestly.", "role": "B"}, {"speaker": "Priya", "text": "Which one are you asking about — how much is this dress?", "role": "A"}, {"speaker": "Arun", "text": "That one.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Teacher", "text": "Let's review our question words today — why, what, when, where, who, how, how much, and which — and I want you to build full sentences using our beginner patterns, like \"My name is [A],\" \"What time is it? It's [A] o'clock,\" \"I like [noun],\" and \"How much is [A]?\"", "role": "A"}, {"speaker": "Student", "text": "My name is Adam, and it's 9 o'clock — I like music, and honestly, I'd ask how much this dress is before deciding, since I'm not sure which one fits my budget.", "role": "B"}, {"speaker": "Teacher", "text": "Good. Now let's also revisit our grammar terms — noun, verb, adjective, particle, adverb, preposition, conjunction, interjection, pronoun, idiom, subject, and object — try using at least three in one sentence about your day.", "role": "A"}, {"speaker": "Student", "text": "As the subject of my own sentence, I'd say the pronoun \"I\" performs the verb \"practiced\" using an adverb like \"carefully,\" while a preposition connects the object — this idiom-heavy exercise is honestly harder than it looks!", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Priya", "text": "Why did you say that, and when did this happen?", "role": "A"}, {"speaker": "Arun", "text": "I don't know why, and it happened yesterday, actually.", "role": "B"}, {"speaker": "Priya", "text": "Where did it happen, and who told you?", "role": "A"}, {"speaker": "Arun", "text": "At the office, and my coworker told me.", "role": "B"}, {"speaker": "Priya", "text": "How did you find out, and how much did it cost?", "role": "A"}, {"speaker": "Arun", "text": "He mentioned it casually. It cost quite a bit, honestly.", "role": "B"}, {"speaker": "Priya", "text": "Which one are you asking about — how much is this dress?", "role": "A"}, {"speaker": "Arun", "text": "That one.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Question Words, Sentence Patterns & Grammar",
            comprehensionQuestion = "What is the main topic discussed in 'Question Words, Sentence Patterns & Grammar'?",
            comprehensionOptions = "Core vocabulary for Question Words, Sentence Patterns & Grammar,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Question Words, Sentence Patterns & Grammar"
        ),
        ConversationSet(
            id = 22,
            title = "Let's Cook in English",
            scenario = "Practice conversational English for 'Let's Cook in English' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "What's in your kitchen?", "role": "A"}, {"speaker": "David", "text": "There is a blender and a bowl.", "role": "B"}, {"speaker": "Sophia", "text": "A coffeemaker?", "role": "A"}, {"speaker": "David", "text": "Yes, and a sink and a cutting board.", "role": "B"}, {"speaker": "Sophia", "text": "A timer and a mixer?", "role": "A"}, {"speaker": "David", "text": "Yes, and a pot, a microwave oven, a stove, a toaster, and a refrigerator.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Chef", "text": "Please add a pinch of salt and black pepper to taste.", "role": "A"}, {"speaker": "Assistant", "text": "I need 1 cup of flour and 2 eggs for this recipe.", "role": "B"}, {"speaker": "Chef", "text": "Also 200 ml of milk, 300 g of sugar, 3 tablespoons of oil, and 1 teaspoon of butter.", "role": "A"}, {"speaker": "Assistant", "text": "Should I stir or slice first?", "role": "B"}, {"speaker": "Chef", "text": "Slice the vegetables, then grill, add the seasoning, chop the herbs, peel the fruit, beat the eggs, and mix everything together.", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Chef", "text": "Before we start, please add a pinch of salt and black pepper to taste, and make sure you have 1 cup of flour, 2 eggs, 200 ml of milk, 300 g of sugar, 3 tablespoons of oil, and 1 teaspoon of butter measured out — I need these for this recipe.", "role": "A"}, {"speaker": "Assistant", "text": "Understood — should I stir the batter first, or slice the fruit and grill the topping while it rests?", "role": "B"}, {"speaker": "Chef", "text": "Slice it thinly, then thinly slice the onions separately, beat the egg whites until stiff, and blend it until smooth before you preheat the oven.", "role": "A"}, {"speaker": "Assistant", "text": "And once that's done?", "role": "B"}, {"speaker": "Chef", "text": "Add the mixture to the cream, chop what's left of the garnish, peel the remaining fruit, mix it in gently, and let it simmer for 30 minutes — we'll use the blender, bowl, coffeemaker, sink, cutting board, timer, mixer, pot, microwave oven, stove, toaster, and refrigerator before we're finished, so let's stay organized.", "role": "A"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Chef", "text": "Please add a pinch of salt and black pepper to taste.", "role": "A"}, {"speaker": "Assistant", "text": "I need 1 cup of flour and 2 eggs for this recipe.", "role": "B"}, {"speaker": "Chef", "text": "Also 200 ml of milk, 300 g of sugar, 3 tablespoons of oil, and 1 teaspoon of butter.", "role": "A"}, {"speaker": "Assistant", "text": "Should I stir or slice first?", "role": "B"}, {"speaker": "Chef", "text": "Slice the vegetables, then grill, add the seasoning, chop the herbs, peel the fruit, beat the eggs, and mix everything together.", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Let's Cook in English",
            comprehensionQuestion = "What is the main topic discussed in 'Let's Cook in English'?",
            comprehensionOptions = "Core vocabulary for Let's Cook in English,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Let's Cook in English"
        ),
        ConversationSet(
            id = 23,
            title = "Talking About Movies & TV",
            scenario = "Practice conversational English for 'Talking About Movies & TV' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Michael", "text": "What kind of movies do you like?", "role": "A"}, {"speaker": "Sarah", "text": "I like horror movies.", "role": "B"}, {"speaker": "Michael", "text": "Comedy or fantasy?", "role": "A"}, {"speaker": "Sarah", "text": "Comedy. Also romance and sci-fi.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "David", "text": "Who's your favorite actor and actress, and do you prefer animation or a dubbed version?", "role": "A"}, {"speaker": "Emma", "text": "I like animation, actually, and I always buy a movie ticket for the premiere at the movie theater rather than waiting for the news to cover it.", "role": "B"}, {"speaker": "David", "text": "What genre is your favorite episode from, and how many seasons does that series have?", "role": "A"}, {"speaker": "Emma", "text": "It's part of a great series, almost a soap opera really, currently on its fourth season, and yes, I watch it with subtitles.", "role": "B"}, {"speaker": "David", "text": "How much does the ticket cost, and what time does the movie start?", "role": "A"}, {"speaker": "Emma", "text": "Twelve dollars, and it starts at 7 PM — some popcorn, please, while we wait for the trailer.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "What kind of movies do you like — horror, comedy, fantasy, romance, or sci-fi? I ask because I'm trying to figure out whether to buy a movie ticket for tonight's premiere or just wait for it to hit a streaming program later.", "role": "A"}, {"speaker": "Michael", "text": "Honestly, I gravitate toward animation and the dubbed version of foreign films rather than reading subtitles, though I'll admit I still watch the news and variety shows out of habit, and I follow at least one soap opera-style series that's now in its fourth season.", "role": "B"}, {"speaker": "Sophia", "text": "Do you know how much the ticket costs, and what time the movie starts? I still need to watch the trailer before deciding whether it's worth choosing over a quiet night with a good episode of something else.", "role": "A"}, {"speaker": "Michael", "text": "It's twelve dollars, starting at 7 PM — grab some popcorn, please, and let's decide once we're at the movie theater, since my favorite actor is apparently in this one, alongside an actress everyone's been talking about lately.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "David", "text": "Who's your favorite actor and actress, and do you prefer animation or a dubbed version?", "role": "A"}, {"speaker": "Emma", "text": "I like animation, actually, and I always buy a movie ticket for the premiere at the movie theater rather than waiting for the news to cover it.", "role": "B"}, {"speaker": "David", "text": "What genre is your favorite episode from, and how many seasons does that series have?", "role": "A"}, {"speaker": "Emma", "text": "It's part of a great series, almost a soap opera really, currently on its fourth season, and yes, I watch it with subtitles.", "role": "B"}, {"speaker": "David", "text": "How much does the ticket cost, and what time does the movie start?", "role": "A"}, {"speaker": "Emma", "text": "Twelve dollars, and it starts at 7 PM — some popcorn, please, while we wait for the trailer.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Talking About Movies & TV",
            comprehensionQuestion = "What is the main topic discussed in 'Talking About Movies & TV'?",
            comprehensionOptions = "Core vocabulary for Talking About Movies & TV,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Talking About Movies & TV"
        ),
        ConversationSet(
            id = 24,
            title = "What's in Your Wallet",
            scenario = "Practice conversational English for 'What's in Your Wallet' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Arun", "text": "What's in your wallet?", "role": "A"}, {"speaker": "Priya", "text": "There's a debit card and cash.", "role": "B"}, {"speaker": "Arun", "text": "A credit card?", "role": "A"}, {"speaker": "Priya", "text": "Yes, and my driver's license.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "David", "text": "Do you carry a wallet, keys, a purse, or your passport with you daily?", "role": "A"}, {"speaker": "Sophia", "text": "I carry a wallet, keys, and my passport, plus an umbrella, my cell phone, a wristwatch, and earphones.", "role": "B"}, {"speaker": "David", "text": "What's your e-mail address, and where are you from?", "role": "A"}, {"speaker": "Sophia", "text": "My e-mail address is maria@email.com, and I'm from Spain.", "role": "B"}, {"speaker": "David", "text": "What's your passport number, for the form?", "role": "A"}, {"speaker": "Sophia", "text": "My passport number is AB123456.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Arun", "text": "What's in your wallet these days — I still keep a point card, a business card, an insurance card, and an identification card in mine, alongside the usual debit card, credit card, cash, and driver's license.", "role": "A"}, {"speaker": "Priya", "text": "I've simplified mine, though I still always carry glasses, gloves, a briefcase, and a camera for work, along with a purse, my passport, an umbrella, a cell phone, a wristwatch, and earphones.", "role": "B"}, {"speaker": "Arun", "text": "This form is asking for everything — name, surname, age, birthday, occupation, address, country, nationality, phone number, and e-mail. It's also asking whether I'm male or female, and single, married, or divorced.", "role": "A"}, {"speaker": "Priya", "text": "Same here — my e-mail address is on file already, and my passport number is AB123456, so filling in \"where are you from\" is the only field I actually have to think about.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "David", "text": "Do you carry a wallet, keys, a purse, or your passport with you daily?", "role": "A"}, {"speaker": "Sophia", "text": "I carry a wallet, keys, and my passport, plus an umbrella, my cell phone, a wristwatch, and earphones.", "role": "B"}, {"speaker": "David", "text": "What's your e-mail address, and where are you from?", "role": "A"}, {"speaker": "Sophia", "text": "My e-mail address is maria@email.com, and I'm from Spain.", "role": "B"}, {"speaker": "David", "text": "What's your passport number, for the form?", "role": "A"}, {"speaker": "Sophia", "text": "My passport number is AB123456.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for What's in Your Wallet",
            comprehensionQuestion = "What is the main topic discussed in 'What's in Your Wallet'?",
            comprehensionOptions = "Core vocabulary for What's in Your Wallet,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for What's in Your Wallet"
        ),
        ConversationSet(
            id = 25,
            title = "Knowing Your Body",
            scenario = "Practice conversational English for 'Knowing Your Body' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Doctor", "text": "This is my ear.", "role": "A"}, {"speaker": "Patient", "text": "This is my mouth.", "role": "B"}, {"speaker": "Doctor", "text": "Nose and hair?", "role": "A"}, {"speaker": "Patient", "text": "Here. Also chin, eye, neck, and tongue.", "role": "B"}, {"speaker": "Doctor", "text": "Cheek, lip, forehead, and eyebrow?", "role": "A"}, {"speaker": "Patient", "text": "All fine.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Doctor", "text": "Does your head hurt, or your shoulder?", "role": "A"}, {"speaker": "Patient", "text": "My hand and chest are fine, but my back hurts near my navel.", "role": "B"}, {"speaker": "Doctor", "text": "What about your finger, foot, leg, or knee?", "role": "A"}, {"speaker": "Patient", "text": "My knee and arm feel stiff, and my ankle is a little swollen.", "role": "B"}, {"speaker": "Doctor", "text": "We'll also check your heart, stomach, lung, and brain.", "role": "A"}, {"speaker": "Patient", "text": "And my liver, kidney, bladder, and blood, please.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Doctor", "text": "Let's go through everything systematically — starting with your face: ear, mouth, nose, hair, chin, eye, neck, tongue, cheek, lip, forehead, and eyebrow all look normal so far.", "role": "A"}, {"speaker": "Patient", "text": "Good to hear. My head, shoulder, hand, and chest feel fine too, though my back has been bothering me near my navel, and my finger and foot have both been a bit numb lately.", "role": "B"}, {"speaker": "Doctor", "text": "We'll want to examine your leg, knee, arm, and ankle as well, then move on to internal organs — heart, stomach, lung, brain, liver, kidney, bladder, and blood work, just to be thorough.", "role": "A"}, {"speaker": "Patient", "text": "While we're at it, could you also test my five senses — taste, sight, hearing, touch, and smell? I've noticed some changes recently that I'd rather rule out early.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Doctor", "text": "Does your head hurt, or your shoulder?", "role": "A"}, {"speaker": "Patient", "text": "My hand and chest are fine, but my back hurts near my navel.", "role": "B"}, {"speaker": "Doctor", "text": "What about your finger, foot, leg, or knee?", "role": "A"}, {"speaker": "Patient", "text": "My knee and arm feel stiff, and my ankle is a little swollen.", "role": "B"}, {"speaker": "Doctor", "text": "We'll also check your heart, stomach, lung, and brain.", "role": "A"}, {"speaker": "Patient", "text": "And my liver, kidney, bladder, and blood, please.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Knowing Your Body",
            comprehensionQuestion = "What is the main topic discussed in 'Knowing Your Body'?",
            comprehensionOptions = "Core vocabulary for Knowing Your Body,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Knowing Your Body"
        ),
        ConversationSet(
            id = 26,
            title = "National Holidays",
            scenario = "Practice conversational English for 'National Holidays' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Woman", "text": "What are you doing for Fourth of July?", "role": "A"}, {"speaker": "Man", "text": "I'm planning to go to the beach.", "role": "B"}, {"speaker": "Woman", "text": "Staying home?", "role": "A"}, {"speaker": "Man", "text": "No, but sometimes I go to the movies or go camping.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Sophia", "text": "Do you celebrate Columbus Day and Inauguration Day?", "role": "A"}, {"speaker": "David", "text": "Yes, and Independence Day, Labor Day, and Memorial Day too.", "role": "B"}, {"speaker": "Sophia", "text": "What about Thanksgiving Day?", "role": "A"}, {"speaker": "David", "text": "Of course. We also visit a church, a mosque, a palace, or a temple sometimes on holidays, with a guidebook in hand.", "role": "B"}, {"speaker": "Sophia", "text": "Do you buy a ticket and take a tour bus, or hire a tour guide?", "role": "A"}, {"speaker": "David", "text": "Usually a tour guide, since we're tourists in most of these places.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Emma", "text": "Before I even think about Columbus Day, Inauguration Day, Independence Day, Labor Day, Memorial Day, or Thanksgiving Day travel, I always choose a destination and request vacation time months in advance.", "role": "A"}, {"speaker": "Michael", "text": "Same here — I buy a guidebook, save money, and apply for a passport early, since booking a flight and accommodations gets expensive closer to the holiday.", "role": "B"}, {"speaker": "Emma", "text": "Once I've packed, bought travel insurance, and gotten a visa if needed, I usually plan visits to a church, mosque, palace, or temple, along with the standard tourist checklist — ticket, tour, tour bus, and tour guide included.", "role": "A"}, {"speaker": "Michael", "text": "This year, though, I'm planning to just go to the beach for Fourth of July instead — no temples, no tour guide, just staying home half the time and maybe going camping or to the movies the rest.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Sophia", "text": "Do you celebrate Columbus Day and Inauguration Day?", "role": "A"}, {"speaker": "David", "text": "Yes, and Independence Day, Labor Day, and Memorial Day too.", "role": "B"}, {"speaker": "Sophia", "text": "What about Thanksgiving Day?", "role": "A"}, {"speaker": "David", "text": "Of course. We also visit a church, a mosque, a palace, or a temple sometimes on holidays, with a guidebook in hand.", "role": "B"}, {"speaker": "Sophia", "text": "Do you buy a ticket and take a tour bus, or hire a tour guide?", "role": "A"}, {"speaker": "David", "text": "Usually a tour guide, since we're tourists in most of these places.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for National Holidays",
            comprehensionQuestion = "What is the main topic discussed in 'National Holidays'?",
            comprehensionOptions = "Core vocabulary for National Holidays,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for National Holidays"
        ),
        ConversationSet(
            id = 27,
            title = "Talking About Numbers",
            scenario = "Practice conversational English for 'Talking About Numbers' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Teacher", "text": "What's your favorite number?", "role": "A"}, {"speaker": "Nana", "text": "It's seven.", "role": "B"}, {"speaker": "Teacher", "text": "One, two, three, four, five, six, seven, eight, nine, ten.", "role": "A"}, {"speaker": "Student", "text": "Got it.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Accountant", "text": "We're talking about 100 — one hundred, and 1,000 — one thousand.", "role": "A"}, {"speaker": "Client", "text": "What about 10,000 and 100,000?", "role": "B"}, {"speaker": "Accountant", "text": "Ten thousand, and one hundred thousand.", "role": "A"}, {"speaker": "Client", "text": "How long will it take to calculate?", "role": "B"}, {"speaker": "Accountant", "text": "It'll take 10 minutes. By the way, how old are you?", "role": "A"}, {"speaker": "Client", "text": "I'm 32 years old.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Teacher", "text": "Today we'll cover the math vocabulary — plus, minus, equals, multiply, divide, half, point, and percent — alongside our large numbers: 100, 1,000, 10,000, 100,000, 1,000,000, 1,000,000,000, and 1,000,000,000,000, meaning one hundred through one trillion.", "role": "A"}, {"speaker": "Student", "text": "So if I have one million and I divide it in half, then multiply by a percent, plus or minus a point here or there, I should land somewhere between one thousand and ten thousand, roughly?", "role": "B"}, {"speaker": "Teacher", "text": "Exactly the kind of thinking we want. Now, practical questions — how long will it take you to finish this exercise, and separately, how old are you, out of curiosity?", "role": "A"}, {"speaker": "Student", "text": "It'll take 10 minutes, probably, and I'm 32 years old — old enough to still get large numbers wrong under pressure, apparently, but my favorite number remains seven.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Accountant", "text": "We're talking about 100 — one hundred, and 1,000 — one thousand.", "role": "A"}, {"speaker": "Client", "text": "What about 10,000 and 100,000?", "role": "B"}, {"speaker": "Accountant", "text": "Ten thousand, and one hundred thousand.", "role": "A"}, {"speaker": "Client", "text": "How long will it take to calculate?", "role": "B"}, {"speaker": "Accountant", "text": "It'll take 10 minutes. By the way, how old are you?", "role": "A"}, {"speaker": "Client", "text": "I'm 32 years old.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Talking About Numbers",
            comprehensionQuestion = "What is the main topic discussed in 'Talking About Numbers'?",
            comprehensionOptions = "Core vocabulary for Talking About Numbers,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Talking About Numbers"
        ),
        ConversationSet(
            id = 28,
            title = "Occupations",
            scenario = "Practice conversational English for 'Occupations' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Interviewer", "text": "What do you do?", "role": "A"}, {"speaker": "Candidate 1", "text": "I'm an artist.", "role": "B"}, {"speaker": "Candidate 2", "text": "I'm a chef.", "role": "B"}, {"speaker": "Candidate 3", "text": "I'm a doctor.", "role": "B"}, {"speaker": "Candidate 4", "text": "I'm a teacher.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Sophia", "text": "Are you a salaried employee, or a freelancer?", "role": "A"}, {"speaker": "Michael", "text": "A freelancer, actually. My brother is a temporary worker, and my sister is unemployed right now. My roommate is a full-time worker, and I also pick up part-time work.", "role": "B"}, {"speaker": "Sophia", "text": "What's your workplace like?", "role": "A"}, {"speaker": "Michael", "text": "There's a good coworker, tight deadlines, regular meetings, and steady teamwork, though I'm overdue for a vacation.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Interviewer", "text": "Tell me about yourself, and while you're at it, tell me about your education.", "role": "A"}, {"speaker": "Candidate", "text": "I'm a company employee currently, though I trained as an engineer — I'm an artist at heart, honestly, having worked as a construction worker and even a photographer before settling into this field.", "role": "B"}, {"speaker": "Interviewer", "text": "Why did you leave your last job, and what are your career goals going forward?", "role": "A"}, {"speaker": "Candidate", "text": "I left because I wanted to move from part-time work toward becoming a full-time worker with real advancement, and eventually I'd like to become a supervisor. Along the way I've known accountants, actors, architects, dentists, graphic designers, hairdressers, journalists, judges, lawyers, nurses, pilots, scientists, singers, students, and writers — and every one of them talked about company, coworker, deadline, meeting, project, salary, teamwork, and vacation the same way I do.", "role": "B"}, {"speaker": "Interviewer", "text": "That's a broad perspective. Are you a police officer, firefighter, mail carrier, or professor by any chance, given your varied background?", "role": "A"}, {"speaker": "Candidate", "text": "None of those, though I respect anyone in those roles — I'm simply a dedicated company employee looking for the next step.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Sophia", "text": "Are you a salaried employee, or a freelancer?", "role": "A"}, {"speaker": "Michael", "text": "A freelancer, actually. My brother is a temporary worker, and my sister is unemployed right now. My roommate is a full-time worker, and I also pick up part-time work.", "role": "B"}, {"speaker": "Sophia", "text": "What's your workplace like?", "role": "A"}, {"speaker": "Michael", "text": "There's a good coworker, tight deadlines, regular meetings, and steady teamwork, though I'm overdue for a vacation.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Occupations",
            comprehensionQuestion = "What is the main topic discussed in 'Occupations'?",
            comprehensionOptions = "Core vocabulary for Occupations,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Occupations"
        ),
        ConversationSet(
            id = 29,
            title = "Talk to Your Pets",
            scenario = "Practice conversational English for 'Talk to Your Pets' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "Do you have any pets?", "role": "A"}, {"speaker": "David", "text": "I have a dog.", "role": "B"}, {"speaker": "Sophia", "text": "A cat too?", "role": "A"}, {"speaker": "David", "text": "Yes, and a hamster.", "role": "B"}, {"speaker": "Sophia", "text": "A rabbit?", "role": "A"}, {"speaker": "David", "text": "Yes, and a goldfish.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Priya", "text": "What breed is your dog?", "role": "A"}, {"speaker": "Arun", "text": "A German Shepherd. My neighbor has a Great Dane, and my sister has a Labrador Retriever.", "role": "B"}, {"speaker": "Priya", "text": "I love Dalmatians, Pugs, and Bulldogs.", "role": "A"}, {"speaker": "Arun", "text": "Don't forget Dachshunds, Yorkshire Terriers, Dobermanns, and Poodles.", "role": "B"}, {"speaker": "Priya", "text": "Can your dog do tricks — roll over, sit, jump, or shake?", "role": "A"}, {"speaker": "Arun", "text": "Yes! Also fetch, lie down, stay, and play dead.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "Do you have any pets beyond the usual dog or cat? I have an iguana, a tarantula, a mouse, a hamster, a rat, a goldfish, and even a parakeet — plus a rabbit, a ferret, a guinea pig, a cat, and a dog, if you can believe it.", "role": "A"}, {"speaker": "David", "text": "My household is more conventional — a German Shepherd, and a Persian cat that acts like she owns the place. I've also known people with Great Danes, Labrador Retrievers, Dalmatians, Pugs, Bulldogs, Dachshunds, Yorkshire Terriers, Dobermanns, and Poodles.", "role": "B"}, {"speaker": "Sophia", "text": "Does your dog respond to commands like roll over, sit, jump, shake, fetch, lie down, stay, or play dead?", "role": "A"}, {"speaker": "David", "text": "All of them, actually. And speaking of cats, I've admired a friend's Maine Coon cat, Siamese cat, Birman cat, Ragdoll cat, Himalayan cat, Sphynx cat, and American Shorthair cat — though I still need to stock up on a collar, a leash, a vaccine, a toy, dog food, cat food, a birdcage, and a hamster ball before my next pet-store trip.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Priya", "text": "What breed is your dog?", "role": "A"}, {"speaker": "Arun", "text": "A German Shepherd. My neighbor has a Great Dane, and my sister has a Labrador Retriever.", "role": "B"}, {"speaker": "Priya", "text": "I love Dalmatians, Pugs, and Bulldogs.", "role": "A"}, {"speaker": "Arun", "text": "Don't forget Dachshunds, Yorkshire Terriers, Dobermanns, and Poodles.", "role": "B"}, {"speaker": "Priya", "text": "Can your dog do tricks — roll over, sit, jump, or shake?", "role": "A"}, {"speaker": "Arun", "text": "Yes! Also fetch, lie down, stay, and play dead.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Talk to Your Pets",
            comprehensionQuestion = "What is the main topic discussed in 'Talk to Your Pets'?",
            comprehensionOptions = "Core vocabulary for Talk to Your Pets,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Talk to Your Pets"
        ),
        ConversationSet(
            id = 30,
            title = "Making a Phone Call",
            scenario = "Practice conversational English for 'Making a Phone Call' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Caller", "text": "Hello, I'd like to speak with the manager.", "role": "A"}, {"speaker": "Receptionist", "text": "Okay, just a moment.", "role": "B"}, {"speaker": "Caller", "text": "Is this customer service?", "role": "A"}, {"speaker": "Receptionist", "text": "Yes.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Caller", "text": "Hello, I'd like to speak with the person in charge.", "role": "A"}, {"speaker": "Receptionist", "text": "Okay, just a moment.", "role": "B"}, {"speaker": "Caller", "text": "I can't hear you very well.", "role": "A"}, {"speaker": "Receptionist", "text": "I'll call you back.", "role": "B"}, {"speaker": "Caller", "text": "I'm free tomorrow. Please answer the phone next time.", "role": "A"}, {"speaker": "Receptionist", "text": "Please wait a moment. The line is busy right now, actually.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Caller", "text": "Hello, I'd like to speak with the person in charge — is that the manager, a sales representative, or should I go through customer service instead?", "role": "A"}, {"speaker": "Receptionist", "text": "Okay, just a moment — I'll check who's available; someone should be able to help.", "role": "B"}, {"speaker": "Caller", "text": "I can't hear you very well — could you try again? I'll call you back if this connection keeps dropping.", "role": "A"}, {"speaker": "Receptionist", "text": "Understood. Please wait a moment while I transfer you — actually, the line is disconnected on his end. What's your phone number, and would you like to leave a message?", "role": "B"}, {"speaker": "Caller", "text": "Sure, go ahead and take it down — by the way, I recently switched to a prepaid mobile phone rather than a monthly contract or installment payment plan, so if the network coverage seems off, that's why. I'm busy this week, but I'm free tomorrow if he wants to call regarding the smartphone's service status, or he can just send a text message.", "role": "A"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Caller", "text": "Hello, I'd like to speak with the person in charge.", "role": "A"}, {"speaker": "Receptionist", "text": "Okay, just a moment.", "role": "B"}, {"speaker": "Caller", "text": "I can't hear you very well.", "role": "A"}, {"speaker": "Receptionist", "text": "I'll call you back.", "role": "B"}, {"speaker": "Caller", "text": "I'm free tomorrow. Please answer the phone next time.", "role": "A"}, {"speaker": "Receptionist", "text": "Please wait a moment. The line is busy right now, actually.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Making a Phone Call",
            comprehensionQuestion = "What is the main topic discussed in 'Making a Phone Call'?",
            comprehensionOptions = "Core vocabulary for Making a Phone Call,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Making a Phone Call"
        ),
        ConversationSet(
            id = 31,
            title = "Asking How to Say Something",
            scenario = "Practice conversational English for 'Asking How to Say Something' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Gabriel", "text": "How do you say this?", "role": "A"}, {"speaker": "Librarian", "text": "It's \"parking lot.\"", "role": "B"}, {"speaker": "Gabriel", "text": "How do you say giraffe?", "role": "A"}, {"speaker": "Librarian", "text": "Giraffe.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Student", "text": "How do you say this word for shop clerk?", "role": "A"}, {"speaker": "Teacher", "text": "It's \"shop clerk.\"", "role": "B"}, {"speaker": "Student", "text": "What about travel and invasion?", "role": "A"}, {"speaker": "Teacher", "text": "Travel and invasion — good words to know.", "role": "B"}, {"speaker": "Student", "text": "Can you help me pronounce breakfast and vocabulary?", "role": "A"}, {"speaker": "Teacher", "text": "Sure, let's practice negotiation and miscellaneous too.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Gabriel", "text": "How do you say this? I keep mixing up words like colleague, hawk, rural, begrime, unfortunately, and realm — some of these are genuinely difficult words to pronounce in English.", "role": "A"}, {"speaker": "Librarian", "text": "It's \"parking lot,\" by the way, for the word you asked about earlier. As for pronunciation practice, try this tongue twister: Betty Botter bought some butter, but she said the butter's bitter.", "role": "B"}, {"speaker": "Gabriel", "text": "That's tough. What about: Peter Piper picked a peck of pickled peppers — a peck of pickled peppers Peter Piper picked? Or how much wood would a woodchuck chuck if a woodchuck could chuck wood?", "role": "A"}, {"speaker": "Librarian", "text": "Even harder — try \"lesser leather never weathered wetter weather better,\" or \"if two witches would watch two watches, which witch would watch which watch?\" And for a real challenge: imagine an imaginary menagerie manager imagining managing an imaginary menagerie.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Student", "text": "How do you say this word for shop clerk?", "role": "A"}, {"speaker": "Teacher", "text": "It's \"shop clerk.\"", "role": "B"}, {"speaker": "Student", "text": "What about travel and invasion?", "role": "A"}, {"speaker": "Teacher", "text": "Travel and invasion — good words to know.", "role": "B"}, {"speaker": "Student", "text": "Can you help me pronounce breakfast and vocabulary?", "role": "A"}, {"speaker": "Teacher", "text": "Sure, let's practice negotiation and miscellaneous too.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Asking How to Say Something",
            comprehensionQuestion = "What is the main topic discussed in 'Asking How to Say Something'?",
            comprehensionOptions = "Core vocabulary for Asking How to Say Something,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Asking How to Say Something"
        ),
        ConversationSet(
            id = 32,
            title = "Romance & Love",
            scenario = "Practice conversational English for 'Romance & Love' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Man", "text": "Hi. Can I sit here?", "role": "A"}, {"speaker": "Woman", "text": "Hello.", "role": "B"}, {"speaker": "Man", "text": "I'm David. Nice to meet you.", "role": "A"}, {"speaker": "Woman", "text": "Nice to meet you too.", "role": "B"}, {"speaker": "Man", "text": "You are so beautiful.", "role": "A"}, {"speaker": "Woman", "text": "Thanks.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Man", "text": "Hi. Can I sit here? I'm David.", "role": "A"}, {"speaker": "Woman", "text": "Hello, nice to meet you.", "role": "B"}, {"speaker": "Man", "text": "May I ask your name? Don't I know you from somewhere?", "role": "A"}, {"speaker": "Woman", "text": "What are you doing here?", "role": "B"}, {"speaker": "Man", "text": "I would like to meet you again. You are so kind and interesting.", "role": "A"}, {"speaker": "Woman", "text": "Thanks. You're pretty handsome and funny yourself.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Man", "text": "Hi. Can I sit here? I'm David — nice to meet you, and may I ask your name? I have this odd feeling — don't I know you from somewhere?", "role": "A"}, {"speaker": "Woman", "text": "Hello, nice to meet you too. What are you doing here, by the way?", "role": "B"}, {"speaker": "Man", "text": "Honestly, I would like to meet you again — you are so beautiful, and also cool, lovely, funny, and interesting all at once, which is a rare combination.", "role": "A"}, {"speaker": "Woman", "text": "Thanks — you're being awfully generous with the compliments, though I'll admit your smile is beautiful too, you're smart, I like your hairstyle, and you have good taste, so I suppose I'll allow \"when do you want to get married\" to remain a joke for now, not a real pick-up line.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Man", "text": "Hi. Can I sit here? I'm David.", "role": "A"}, {"speaker": "Woman", "text": "Hello, nice to meet you.", "role": "B"}, {"speaker": "Man", "text": "May I ask your name? Don't I know you from somewhere?", "role": "A"}, {"speaker": "Woman", "text": "What are you doing here?", "role": "B"}, {"speaker": "Man", "text": "I would like to meet you again. You are so kind and interesting.", "role": "A"}, {"speaker": "Woman", "text": "Thanks. You're pretty handsome and funny yourself.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Romance & Love",
            comprehensionQuestion = "What is the main topic discussed in 'Romance & Love'?",
            comprehensionOptions = "Core vocabulary for Romance & Love,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Romance & Love"
        ),
        ConversationSet(
            id = 33,
            title = "Singing in English",
            scenario = "Practice conversational English for 'Singing in English' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Teacher", "text": "What musical instrument can you play?", "role": "A"}, {"speaker": "Student 1", "text": "I can play the piano.", "role": "B"}, {"speaker": "Student 2", "text": "I can play the guitar.", "role": "B"}, {"speaker": "Student 3", "text": "I can play the violin.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Priya", "text": "What kind of music do you like — pop or house music?", "role": "A"}, {"speaker": "Arun", "text": "Techno and funk, mostly. My brother likes hip hop, R&B, rock 'n' roll, and rap.", "role": "B"}, {"speaker": "Priya", "text": "What instrument do you play?", "role": "A"}, {"speaker": "Arun", "text": "The accordion and the viola, actually. I also dabble with the harp and French horn.", "role": "B"}, {"speaker": "Priya", "text": "My favorite song is \"Imagine,\" and my favorite singer is Adele.", "role": "A"}, {"speaker": "Arun", "text": "I can sing very well myself. Do you want to go to karaoke?", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Teacher", "text": "Between the piano, guitar, accordion, viola, violin, harp, French horn, cello, clarinet, saxophone, keyboard, and trumpet, which instruments have you actually mastered, rather than just dabbled in?", "role": "A"}, {"speaker": "Student", "text": "I can play the piano and the cello competently, though I'm still working on the saxophone. As for taste, I gravitate toward pop, house music, and techno, but I'll admit a soft spot for funk, hip hop, R&B, rock 'n' roll, and rap depending on my mood.", "role": "B"}, {"speaker": "Teacher", "text": "My favorite song changes constantly, but my favorite singer has always been consistent — I love music generally, and I can sing very well when I let myself. Do you want to go to karaoke sometime and put the chorus, solo, soprano, alto, tenor, melody, and rhythm to the test?", "role": "A"}, {"speaker": "Student", "text": "Absolutely — and speaking of legends, I'd want a set list spanning Elvis Presley, Michael Jackson, Taylor Swift, Lady Gaga, Nicki Minaj, Johnny Cash, Jimi Hendrix, Tupac Shakur, Eminem, and 50 Cent, if the karaoke machine can handle that range.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Priya", "text": "What kind of music do you like — pop or house music?", "role": "A"}, {"speaker": "Arun", "text": "Techno and funk, mostly. My brother likes hip hop, R&B, rock 'n' roll, and rap.", "role": "B"}, {"speaker": "Priya", "text": "What instrument do you play?", "role": "A"}, {"speaker": "Arun", "text": "The accordion and the viola, actually. I also dabble with the harp and French horn.", "role": "B"}, {"speaker": "Priya", "text": "My favorite song is \"Imagine,\" and my favorite singer is Adele.", "role": "A"}, {"speaker": "Arun", "text": "I can sing very well myself. Do you want to go to karaoke?", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Singing in English",
            comprehensionQuestion = "What is the main topic discussed in 'Singing in English'?",
            comprehensionOptions = "Core vocabulary for Singing in English,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Singing in English"
        ),
        ConversationSet(
            id = 34,
            title = "Sports and Exercise",
            scenario = "Practice conversational English for 'Sports and Exercise' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Priya", "text": "What sports do you like?", "role": "A"}, {"speaker": "Arun", "text": "I like baseball.", "role": "B"}, {"speaker": "Priya", "text": "Do you like to work out?", "role": "A"}, {"speaker": "Arun", "text": "Yes, weightlifting.", "role": "B"}, {"speaker": "Priya", "text": "Basketball or soccer?", "role": "A"}, {"speaker": "Arun", "text": "Soccer.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Priya", "text": "What sports do you like — ice skating, archery, golf?", "role": "A"}, {"speaker": "Arun", "text": "I like weightlifting and track and field. Do you like to work out too?", "role": "B"}, {"speaker": "Priya", "text": "Yes, and I go to the gym regularly. I'm a gym member, and I always start with a warm-up exercise and stretching before I sweat.", "role": "A"}, {"speaker": "Arun", "text": "Do you use a towel and shower after?", "role": "B"}, {"speaker": "Priya", "text": "Of course, and I check the scale to see if I'm trying to gain weight or lose weight.", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Priya", "text": "Between ice skating, archery, baseball, golf, weightlifting, track and field, bowling, tennis, volleyball, badminton, basketball, and soccer, I honestly can't pick a favorite — though I lean toward tennis and volleyball when I actually want a workout.", "role": "A"}, {"speaker": "Arun", "text": "I'm more of a gym person myself — being a gym member means I always start with a warm-up exercise and some stretching before I sweat, then finish with a towel and shower, checking the scale afterward depending on whether I'm trying to gain weight or lose weight that month.", "role": "B"}, {"speaker": "Priya", "text": "For cardio, do you prefer walking, running, the treadmill, or the bike, and do you mix in jumping, weight training, yoga, or pilates?", "role": "A"}, {"speaker": "Arun", "text": "All of the above, plus dancing and strength training on the machine with dumbbells — mostly targeting my abs, arms, shoulders, chest, knees, back, thighs, and calves. And when I actually compete, I'm proud to say I've taken first place and a gold medal twice, though second place with a silver medal, or even third place with a bronze medal, still beats being the loser rather than the winner.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Priya", "text": "What sports do you like — ice skating, archery, golf?", "role": "A"}, {"speaker": "Arun", "text": "I like weightlifting and track and field. Do you like to work out too?", "role": "B"}, {"speaker": "Priya", "text": "Yes, and I go to the gym regularly. I'm a gym member, and I always start with a warm-up exercise and stretching before I sweat.", "role": "A"}, {"speaker": "Arun", "text": "Do you use a towel and shower after?", "role": "B"}, {"speaker": "Priya", "text": "Of course, and I check the scale to see if I'm trying to gain weight or lose weight.", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Sports and Exercise",
            comprehensionQuestion = "What is the main topic discussed in 'Sports and Exercise'?",
            comprehensionOptions = "Core vocabulary for Sports and Exercise,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Sports and Exercise"
        ),
        ConversationSet(
            id = 35,
            title = "At the Supermarket",
            scenario = "Practice conversational English for 'At the Supermarket' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Clerk", "text": "What are you looking for?", "role": "A"}, {"speaker": "Sophia", "text": "I'm looking for the bakery.", "role": "B"}, {"speaker": "Clerk", "text": "The frozen section?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, and produce.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Clerk", "text": "What are you looking for?", "role": "A"}, {"speaker": "Sophia", "text": "I'm looking for a shopping basket, and the frozen section.", "role": "B"}, {"speaker": "Clerk", "text": "Anything else — the bakery, beverages, baking supplies?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, and condiments, the deli, produce, canned food, and snacks.", "role": "B"}, {"speaker": "Clerk", "text": "Need apples, oranges, or strawberries from produce?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, and watermelons, bananas, pineapples, cabbage, mushrooms, onions, potatoes, cucumbers, and carrots.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Clerk", "text": "What are you looking for today — a shopping basket or a shopping cart? Given your list, I'd guess the cart, since you'll need the frozen section, the bakery, beverages, baking supplies, condiments, the deli, produce, canned food, snacks, and the floral department.", "role": "A"}, {"speaker": "Sophia", "text": "Exactly right — for produce I need apples, oranges, strawberries, watermelons, bananas, pineapples, cabbage, mushrooms, onions, potatoes, cucumbers, and carrots, and I'll be checking the price, date processed, use by date, and calories on everything.", "role": "B"}, {"speaker": "Clerk", "text": "Good habit — also worth checking if it's organic, reading the nutrition facts, confirming it's gluten free, and noting the weight before you buy.", "role": "A"}, {"speaker": "Sophia", "text": "There's a sale today too, isn't there — 20% off, or $3 off, and I heard it's buy one, get one free on some items, which will definitely save money whether I pay with cash, a credit card, or a debit card.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Clerk", "text": "What are you looking for?", "role": "A"}, {"speaker": "Sophia", "text": "I'm looking for a shopping basket, and the frozen section.", "role": "B"}, {"speaker": "Clerk", "text": "Anything else — the bakery, beverages, baking supplies?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, and condiments, the deli, produce, canned food, and snacks.", "role": "B"}, {"speaker": "Clerk", "text": "Need apples, oranges, or strawberries from produce?", "role": "A"}, {"speaker": "Sophia", "text": "Yes, and watermelons, bananas, pineapples, cabbage, mushrooms, onions, potatoes, cucumbers, and carrots.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for At the Supermarket",
            comprehensionQuestion = "What is the main topic discussed in 'At the Supermarket'?",
            comprehensionOptions = "Core vocabulary for At the Supermarket,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for At the Supermarket"
        ),
        ConversationSet(
            id = 36,
            title = "Talking Online",
            scenario = "Practice conversational English for 'Talking Online' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "I'd like to buy a smartphone.", "role": "A"}, {"speaker": "Clerk", "text": "Do you need a laptop too?", "role": "B"}, {"speaker": "Sophia", "text": "Yes, and a mouse and keyboard.", "role": "A"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Sophia", "text": "I'd like to buy a smartphone, a laptop, and a router.", "role": "A"}, {"speaker": "Clerk", "text": "Do you need a flash drive or an optical drive too?", "role": "B"}, {"speaker": "Sophia", "text": "Yes, and a mouse, keyboard, monitor, and tablet.", "role": "A"}, {"speaker": "Clerk", "text": "What about a webcam or computer case?", "role": "B"}, {"speaker": "Sophia", "text": "Both, please, along with a sound card.", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "I'd like to buy a smartphone, but do you have a flash drive, router, optical drive, mouse, laptop, sound card, keyboard, monitor, tablet, webcam, and computer case in stock as well? I'm essentially rebuilding my whole setup.", "role": "A"}, {"speaker": "Clerk", "text": "We do. And once you're set up, are you the type to search for images, click every link, and like or share posts, or do you mostly just upload and download files and follow a few hashtags?", "role": "B"}, {"speaker": "Sophia", "text": "A bit of both — lol, brb, btw, lmk, g2g, DM, TBH, IMO — I use all of that shorthand constantly. What's your favorite website, by the way? Are you on Facebook, Instagram, Snapchat, WhatsApp, Skype, Pinterest, LinkedIn, or Twitter?", "role": "A"}, {"speaker": "Clerk", "text": "Mostly Instagram and LinkedIn. Please like my photo if you follow me, and feel free to share this if you like it — just let me know how to upload this picture or download that file if you need help once everything's connected.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Sophia", "text": "I'd like to buy a smartphone, a laptop, and a router.", "role": "A"}, {"speaker": "Clerk", "text": "Do you need a flash drive or an optical drive too?", "role": "B"}, {"speaker": "Sophia", "text": "Yes, and a mouse, keyboard, monitor, and tablet.", "role": "A"}, {"speaker": "Clerk", "text": "What about a webcam or computer case?", "role": "B"}, {"speaker": "Sophia", "text": "Both, please, along with a sound card.", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Talking Online",
            comprehensionQuestion = "What is the main topic discussed in 'Talking Online'?",
            comprehensionOptions = "Core vocabulary for Talking Online,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Talking Online"
        ),
        ConversationSet(
            id = 37,
            title = "Planning Your Time",
            scenario = "Practice conversational English for 'Planning Your Time' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "What day is it?", "role": "A"}, {"speaker": "David", "text": "It's Sunday.", "role": "B"}, {"speaker": "Sophia", "text": "Do you have plans on Monday?", "role": "A"}, {"speaker": "David", "text": "No.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Sophia", "text": "What day is it, and do you have any plans on Tuesday or Wednesday?", "role": "A"}, {"speaker": "David", "text": "It's Monday, and no plans yet. What time is it — the hour and minute?", "role": "B"}, {"speaker": "Sophia", "text": "It's 10 AM, almost noon, not PM yet.", "role": "A"}, {"speaker": "David", "text": "I love spring and summer, and my sister loves autumn/fall and winter.", "role": "B"}, {"speaker": "Sophia", "text": "What time, day of the week, and season is it — this month or next month?", "role": "A"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "What day is it — and more importantly, do you have any plans on Thursday, Friday, or Saturday? I'm trying to plan around this week versus next week without stepping on last week's leftover tasks.", "role": "A"}, {"speaker": "David", "text": "It's Wednesday, and I'm free. Let's meet at 5 o'clock — 5 minutes before or 5 minutes after works fine, and we can grab coffee for about 5 hours if the conversation runs long.", "role": "B"}, {"speaker": "Sophia", "text": "Sounds good. Between January, February, March, April, May, June, July, August, September, October, November, and December, which month works best for the bigger trip — and do you prefer spring, summer, autumn/fall, or winter travel?", "role": "A"}, {"speaker": "David", "text": "I'd lean toward spring or autumn/fall, honestly, sometime this year rather than next year, since last year's vacation got completely swallowed by holiday scheduling and I don't want that mistake to repeat itself this weekend or the next.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Sophia", "text": "What day is it, and do you have any plans on Tuesday or Wednesday?", "role": "A"}, {"speaker": "David", "text": "It's Monday, and no plans yet. What time is it — the hour and minute?", "role": "B"}, {"speaker": "Sophia", "text": "It's 10 AM, almost noon, not PM yet.", "role": "A"}, {"speaker": "David", "text": "I love spring and summer, and my sister loves autumn/fall and winter.", "role": "B"}, {"speaker": "Sophia", "text": "What time, day of the week, and season is it — this month or next month?", "role": "A"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Planning Your Time",
            comprehensionQuestion = "What is the main topic discussed in 'Planning Your Time'?",
            comprehensionOptions = "Core vocabulary for Planning Your Time,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Planning Your Time"
        ),
        ConversationSet(
            id = 38,
            title = "Travel to the USA",
            scenario = "Practice conversational English for 'Travel to the USA' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Tourist", "text": "Is there an ATM nearby?", "role": "A"}, {"speaker": "Local", "text": "Yes, right there.", "role": "B"}, {"speaker": "Tourist", "text": "How much is this?", "role": "A"}, {"speaker": "Vendor", "text": "Ten dollars.", "role": "B"}, {"speaker": "Tourist", "text": "Where's the station?", "role": "A"}, {"speaker": "Local", "text": "Down the street.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Tourist", "text": "Is there an ATM nearby? How much is this — twenty dollars?", "role": "A"}, {"speaker": "Vendor", "text": "Yes, or ten dollars, five dollars, two dollars, or one dollar for the smaller items.", "role": "B"}, {"speaker": "Tourist", "text": "I'd like to go to Times Square. Where's that?", "role": "A"}, {"speaker": "Guide", "text": "New York City. Please take me to the hotel afterward, if you don't mind.", "role": "B"}, {"speaker": "Driver", "text": "Sure. Where's the restroom, you asked earlier — just down the hall.", "role": "B"}, {"speaker": "Tourist", "text": "A hamburger, please. What do you recommend?", "role": "A"}, {"speaker": "Vendor", "text": "Try the hot dog or the Smithfield ham.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Tourist", "text": "Is there an ATM nearby? I need to break a one hundred dollar bill into fifties, twenties, tens, fives, twos, and ones, plus some change — half dollar and quarter dollar coins if you have them.", "role": "A"}, {"speaker": "Local", "text": "There's one near Faneuil Hall Marketplace in Boston, Massachusetts, actually — not far from Navy Pier in Chicago, Illinois, if you happen to be traveling that way too, or Fisherman's Wharf out in San Francisco.", "role": "B"}, {"speaker": "Tourist", "text": "I'd like to go to Times Square and Disneyland Park eventually, but first — do you understand me? I don't understand this menu at all, and I don't speak much English.", "role": "A"}, {"speaker": "Local", "text": "I understand you fine — do you speak English? Yes, I do, though I don't understand every regional phrase myself. Can you eat this? Of course, unless you can't eat it for allergy reasons.", "role": "B"}, {"speaker": "Tourist", "text": "A hamburger, please, and what do you recommend besides that — the hot dog, Smithfield ham, Boston baked beans, bacon and eggs, apple pie, grits, or Jelly Belly Candy? One, two, three, four, five, six, seven, eight, nine, ten — I'll take ten of the candy, please, hello, excuse me, and thank you for your patience; I'm sorry for all the questions, but nice to meet you, and yes, I appreciate the help.", "role": "A"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Tourist", "text": "Is there an ATM nearby? How much is this — twenty dollars?", "role": "A"}, {"speaker": "Vendor", "text": "Yes, or ten dollars, five dollars, two dollars, or one dollar for the smaller items.", "role": "B"}, {"speaker": "Tourist", "text": "I'd like to go to Times Square. Where's that?", "role": "A"}, {"speaker": "Guide", "text": "New York City. Please take me to the hotel afterward, if you don't mind.", "role": "B"}, {"speaker": "Driver", "text": "Sure. Where's the restroom, you asked earlier — just down the hall.", "role": "B"}, {"speaker": "Tourist", "text": "A hamburger, please. What do you recommend?", "role": "A"}, {"speaker": "Vendor", "text": "Try the hot dog or the Smithfield ham.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Travel to the USA",
            comprehensionQuestion = "What is the main topic discussed in 'Travel to the USA'?",
            comprehensionOptions = "Core vocabulary for Travel to the USA,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Travel to the USA"
        ),
        ConversationSet(
            id = 39,
            title = "Useful Verbs",
            scenario = "Practice conversational English for 'Useful Verbs' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "David", "text": "I like to cook.", "role": "A"}, {"speaker": "Emma", "text": "I like to eat.", "role": "B"}, {"speaker": "David", "text": "Do you drink coffee?", "role": "A"}, {"speaker": "Emma", "text": "Yes, and I listen to music.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "David", "text": "I like to cook and drink coffee in the morning.", "role": "A"}, {"speaker": "Emma", "text": "I prefer to eat breakfast while I listen to music.", "role": "B"}, {"speaker": "David", "text": "Do you nap or shop in the afternoon?", "role": "A"}, {"speaker": "Emma", "text": "Sometimes I nap, and I love to shop, sightsee, and sing.", "role": "B"}, {"speaker": "David", "text": "What are you doing right now?", "role": "A"}, {"speaker": "Emma", "text": "I'm watching TV. What will you do tomorrow?", "role": "B"}, {"speaker": "David", "text": "I will travel. What did you do yesterday?", "role": "A"}, {"speaker": "Emma", "text": "I went to the cinema.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "David", "text": "I like to cook, drink coffee, eat breakfast, and listen to music most mornings, though I'll occasionally nap, shop, sightsee, or sing if the day allows for it — otherwise I stretch, think, wait, and walk just to clear my head.", "role": "A"}, {"speaker": "Emma", "text": "Our textbook covers call, can, come, cut, do, go, help, make, return, ride, see, and use — could you use three of those in one sentence about your week?", "role": "B"}, {"speaker": "David", "text": "Sure — I'll call a friend, go help her move, and return the favor by riding along afterward. As for language study, I ask, hear, read, and speak daily, though I only study, teach, understand, and write when I'm feeling ambitious.", "role": "A"}, {"speaker": "Emma", "text": "Good discipline. And remember our opposites — open versus close, begin versus finish, push versus pull, turn on versus turn off — try weaving a few into your next journal entry. What are you doing right now, by the way, what will you do tomorrow, and what did you do yesterday?", "role": "B"}, {"speaker": "David", "text": "Right now, I'm watching TV; tomorrow, I will travel; and yesterday, I went to the cinema — proof that even simple verbs can carry a whole week's story.", "role": "A"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "David", "text": "I like to cook and drink coffee in the morning.", "role": "A"}, {"speaker": "Emma", "text": "I prefer to eat breakfast while I listen to music.", "role": "B"}, {"speaker": "David", "text": "Do you nap or shop in the afternoon?", "role": "A"}, {"speaker": "Emma", "text": "Sometimes I nap, and I love to shop, sightsee, and sing.", "role": "B"}, {"speaker": "David", "text": "What are you doing right now?", "role": "A"}, {"speaker": "Emma", "text": "I'm watching TV. What will you do tomorrow?", "role": "B"}, {"speaker": "David", "text": "I will travel. What did you do yesterday?", "role": "A"}, {"speaker": "Emma", "text": "I went to the cinema.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Useful Verbs",
            comprehensionQuestion = "What is the main topic discussed in 'Useful Verbs'?",
            comprehensionOptions = "Core vocabulary for Useful Verbs,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Useful Verbs"
        ),
        ConversationSet(
            id = 40,
            title = "How's the Weather?",
            scenario = "Practice conversational English for 'How's the Weather?' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "How's the weather?", "role": "A"}, {"speaker": "David", "text": "It's sunny.", "role": "B"}, {"speaker": "Sophia", "text": "Was it rainy yesterday?", "role": "A"}, {"speaker": "David", "text": "Yes, and windy.", "role": "B"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Sophia", "text": "How's the weather today — sunny or cloudy?", "role": "A"}, {"speaker": "David", "text": "Sunny, though yesterday it was cloudy and rainy, then windy, misty, and snowy by evening.", "role": "B"}, {"speaker": "Sophia", "text": "Is it hot or cold now?", "role": "A"}, {"speaker": "David", "text": "It's hot now, though this morning it was warm, then cool, and clear before that.", "role": "B"}, {"speaker": "Sophia", "text": "Should I bring a hat, gloves, and sunscreen, or an umbrella and raincoat?", "role": "A"}, {"speaker": "David", "text": "Bring both — also sunglasses, a scarf, and rain boots, just in case.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "How's the weather looking for the trip — sunny, cloudy, rainy, windy, misty, snowy, stormy, or clear? The forecast keeps flipping between hot, warm, cool, and cold depending on the hour.", "role": "A"}, {"speaker": "David", "text": "They're predicting a blizzard early on, with cloud cover, fog, and unexpected heat later — plus a hurricane warning, some ice, lightning, and mist scattered through the week, followed by rain, a shower, snow, a storm, sun, thunder, a possible tornado, and steady wind, which is a lot for one forecast.", "role": "B"}, {"speaker": "Sophia", "text": "Given all that, I'll pack a hat, gloves, a parasol, rain boots, a raincoat, a scarf, sunglasses, sunscreen, and an umbrella, and I'll keep checking the temperature — in degrees Celsius and Fahrenheit — to see whether we're above zero or below zero before we leave.", "role": "A"}, {"speaker": "David", "text": "Good call. What's it like outside right now, and what's the actual weather forecast say — it seems like it's going to rain, based on the temperature drop.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Sophia", "text": "How's the weather today — sunny or cloudy?", "role": "A"}, {"speaker": "David", "text": "Sunny, though yesterday it was cloudy and rainy, then windy, misty, and snowy by evening.", "role": "B"}, {"speaker": "Sophia", "text": "Is it hot or cold now?", "role": "A"}, {"speaker": "David", "text": "It's hot now, though this morning it was warm, then cool, and clear before that.", "role": "B"}, {"speaker": "Sophia", "text": "Should I bring a hat, gloves, and sunscreen, or an umbrella and raincoat?", "role": "A"}, {"speaker": "David", "text": "Bring both — also sunglasses, a scarf, and rain boots, just in case.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for How's the Weather?",
            comprehensionQuestion = "What is the main topic discussed in 'How's the Weather?'?",
            comprehensionOptions = "Core vocabulary for How's the Weather?,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for How's the Weather?"
        ),
        ConversationSet(
            id = 41,
            title = "Leisure Time Activities",
            scenario = "Practice conversational English for 'Leisure Time Activities' across Basic, Intermediate, and Advanced levels.",
            basicDialogueJson = """[{"speaker": "Sophia", "text": "What do you like to do in your free time?", "role": "A"}, {"speaker": "David", "text": "I like to read.", "role": "B"}, {"speaker": "Sophia", "text": "I like to paint.", "role": "A"}, {"speaker": "David", "text": "Do you watch movies?", "role": "B"}, {"speaker": "Sophia", "text": "Yes, and I ride a bike.", "role": "A"}]""".trimIndent(),
            intermediateDialogueJson = """[{"speaker": "Sophia", "text": "What do you like to do in your free time — play video games or take pictures?", "role": "A"}, {"speaker": "David", "text": "I like to surf the internet and paint. What about you?", "role": "B"}, {"speaker": "Sophia", "text": "I like to read, watch movies, and play an instrument.", "role": "A"}, {"speaker": "David", "text": "I prefer to ride a bike, listen to music, go shopping, go camping, and play sports.", "role": "B"}, {"speaker": "Sophia", "text": "What are you doing this weekend?", "role": "A"}, {"speaker": "David", "text": "I'm going to go to the movies, or maybe go to a concert.", "role": "B"}]""".trimIndent(),
            advancedDialogueJson = """[{"speaker": "Sophia", "text": "In your free time, do you lean toward playing video games, taking pictures, surfing the internet, painting, reading, watching movies, playing an instrument, riding a bike, listening to music, going shopping, going camping, or playing sports — or some combination of all of it?", "role": "A"}, {"speaker": "David", "text": "Mostly reading and watching movies, honestly — I love comedy and science fiction, but I'll happily sit through romance, thriller, action, fantasy, musical, horror, drama, documentary, animation, or mystery if the reviews are good.", "role": "B"}, {"speaker": "Sophia", "text": "What are you doing this weekend, then — going to the movies, going to a concert, hitting an amusement park, going to karaoke, staying home to watch TV, going to the park, going to the theater, or traveling somewhere new?", "role": "A"}, {"speaker": "David", "text": "Probably going to the movies — I still haven't seen \"The Sixth Sense,\" \"Aliens,\" \"Blackfish,\" \"The Nightmare Before Christmas,\" or \"The Princess Bride,\" so it's officially time to catch up on some must-see American movies.", "role": "B"}]""".trimIndent(),
            dialogueJson = """[{"speaker": "Sophia", "text": "What do you like to do in your free time — play video games or take pictures?", "role": "A"}, {"speaker": "David", "text": "I like to surf the internet and paint. What about you?", "role": "B"}, {"speaker": "Sophia", "text": "I like to read, watch movies, and play an instrument.", "role": "A"}, {"speaker": "David", "text": "I prefer to ride a bike, listen to music, go shopping, go camping, and play sports.", "role": "B"}, {"speaker": "Sophia", "text": "What are you doing this weekend?", "role": "A"}, {"speaker": "David", "text": "I'm going to go to the movies, or maybe go to a concert.", "role": "B"}]""".trimIndent(),
            vocabularyCallout = "Basic Expressions, Intermediate Phrases, Advanced Vocabulary for Leisure Time Activities",
            comprehensionQuestion = "What is the main topic discussed in 'Leisure Time Activities'?",
            comprehensionOptions = "Core vocabulary for Leisure Time Activities,Unrelated grammar rules,Weather forecasts,Cooking recipes",
            comprehensionAnswer = "Core vocabulary for Leisure Time Activities"
        )
    )

    val dailySentences = listOf(
        // Greetings
        DailySentence(1, "Hi there! How's your day going so far?", "", "Greetings", "Beginner", soundFocus = "h"),
        DailySentence(2, "Good morning! Did you catch the early train today?", "", "Greetings", "Beginner", soundFocus = "g"),
        DailySentence(3, "What a wonderful day for a long walk outside!", "", "Greetings", "Beginner", soundFocus = "w"),
        DailySentence(4, "Hello! I hope you are having a wonderful start to your week.", "", "Greetings", "Beginner", soundFocus = "h"),
        DailySentence(5, "How are things going with your new office layout?", "", "Greetings", "Intermediate", soundFocus = "th"),
        DailySentence(6, "It's great to see you again! How have you been?", "", "Greetings", "Intermediate", soundFocus = "g"),
        DailySentence(7, "It is an absolute honor to introduce our keynote speaker.", "", "Greetings", "Advanced", soundFocus = "h"),
        DailySentence(8, "Allow me to express my sincerest gratitude for your contribution.", "", "Greetings", "Advanced", soundFocus = "ex"),

        // Requests
        DailySentence(9, "Could you please pass me that blue folder?", "", "Requests", "Beginner", soundFocus = "p"),
        DailySentence(10, "Sorry to interrupt, but I have a quick question.", "", "Requests", "Intermediate", soundFocus = "s"),
        DailySentence(11, "Could you please tell me where the closest subway is?", "", "Requests", "Intermediate", soundFocus = "cl"),
        DailySentence(12, "Could you please give me a hand with this heavy box?", "", "Requests", "Beginner", soundFocus = "h"),
        DailySentence(13, "Excuse me, do you happen to know what time the library closes?", "", "Requests", "Intermediate", soundFocus = "x"),
        DailySentence(14, "Could you please clarify what you meant by that statement?", "", "Requests", "Intermediate", soundFocus = "cl"),
        DailySentence(15, "Could you tell me how to get to the main lobby?", "", "Requests", "Beginner", soundFocus = "l"),
        DailySentence(16, "Would you mind holding this folder for a second?", "", "Requests", "Beginner", soundFocus = "h"),
        DailySentence(17, "Do you think we can schedule a quick call to review?", "", "Requests", "Intermediate", soundFocus = "sch"),
        DailySentence(18, "Could you kindly send over the updated spreadsheet by tomorrow?", "", "Requests", "Intermediate", soundFocus = "k"),
        DailySentence(19, "I'd appreciate it if you could provide some feedback on this.", "", "Requests", "Advanced", soundFocus = "pr"),
        DailySentence(20, "Would it be possible for you to expedite this delivery?", "", "Requests", "Advanced", soundFocus = "ex"),

        // Opinions
        DailySentence(21, "I honestly have no idea what we should eat.", "", "Opinions", "Beginner", soundFocus = "n"),
        DailySentence(22, "That sounds like a wonderful opportunity for you!", "", "Opinions", "Beginner", soundFocus = "w"),
        DailySentence(23, "What are your thoughts on this design proposal?", "", "Opinions", "Intermediate", soundFocus = "th"),
        DailySentence(24, "To be frank, I think we need to rethink our strategy.", "", "Opinions", "Advanced", soundFocus = "f"),
        DailySentence(25, "I think we should take that recommendation with a grain of salt.", "", "Opinions", "Advanced", soundFocus = "v"),
        DailySentence(26, "In my opinion, this restaurant serves the best pizza in town.", "", "Opinions", "Beginner", soundFocus = "p"),
        DailySentence(27, "What are your initial impressions of our new project guidelines?", "", "Opinions", "Intermediate", soundFocus = "pr"),
        DailySentence(28, "To be perfectly honest, I think we should explore other alternatives.", "", "Opinions", "Advanced", soundFocus = "h"),
        DailySentence(29, "We should probably take these rumors with a grain of salt.", "", "Opinions", "Advanced", soundFocus = "r"),
        DailySentence(30, "I believe we should give this new method a try.", "", "Opinions", "Beginner", soundFocus = "b"),
        DailySentence(31, "To be honest, I really like the color of your new car.", "", "Opinions", "Beginner", soundFocus = "h"),
        DailySentence(32, "As far as I'm concerned, we are on the right track.", "", "Opinions", "Intermediate", soundFocus = "c"),
        DailySentence(33, "I'm of the opinion that we should postpone the launch.", "", "Opinions", "Intermediate", soundFocus = "p"),
        DailySentence(34, "I am strongly convinced that we must pivot our target market.", "", "Opinions", "Advanced", soundFocus = "p"),
        DailySentence(35, "I contend that our current approach is no longer sustainable.", "", "Opinions", "Advanced", soundFocus = "s"),

        // Social
        DailySentence(36, "Let's grab a quick bite before the movie starts.", "", "Social", "Beginner", soundFocus = "b"),
        DailySentence(37, "I really appreciate your helping me out with this project.", "", "Social", "Intermediate", soundFocus = "r"),
        DailySentence(38, "Let's schedule a brief sync to iron out these details.", "", "Social", "Advanced", soundFocus = "s"),
        DailySentence(39, "Let's check out that new coffee shop on the corner.", "", "Social", "Beginner", soundFocus = "ch"),
        DailySentence(40, "I'm looking forward to our weekend hiking trip next week.", "", "Social", "Intermediate", soundFocus = "f"),
        DailySentence(41, "Let's set up a quick call tomorrow to touch base on these items.", "", "Social", "Advanced", soundFocus = "t"),
        DailySentence(42, "Let's grab some lunch after we wrap up this meeting.", "", "Social", "Beginner", soundFocus = "l"),
        DailySentence(43, "Let's join the team for dinner tonight if you're free.", "", "Social", "Beginner", soundFocus = "j"),
        DailySentence(44, "Let's arrange a brief get-together to celebrate her promotion.", "", "Social", "Intermediate", soundFocus = "t"),
        DailySentence(45, "Let's organize a quick sync to clear up any confusion.", "", "Social", "Intermediate", soundFocus = "s"),
        DailySentence(46, "Let's convene a steering committee to deliberate on this issue.", "", "Social", "Advanced", soundFocus = "c"),
        DailySentence(47, "Let's coordinate a strategy session to outline the roadmap.", "", "Social", "Advanced", soundFocus = "c"),

        // Emotions
        DailySentence(48, "I'm looking forward to visiting our parents next week.", "", "Emotions", "Intermediate", soundFocus = "r"),
        DailySentence(49, "I'm absolutely thrilled to welcome you to our core team!", "", "Emotions", "Advanced", soundFocus = "th"),
        DailySentence(50, "I was under the impression that the project was already done.", "", "Emotions", "Advanced", soundFocus = "sh"),
        DailySentence(51, "I'm absolutely delighted that you could make it to my birthday party.", "", "Emotions", "Beginner", soundFocus = "d"),
        DailySentence(52, "I've been feeling a bit overwhelmed with work lately.", "", "Emotions", "Intermediate", soundFocus = "v"),
        DailySentence(53, "I'm incredibly grateful for your unwavering support during this challenging transition.", "", "Emotions", "Advanced", soundFocus = "g"),
        DailySentence(54, "I was under the impression that we had more time to submit our proposal.", "", "Emotions", "Advanced", soundFocus = "sub"),
        DailySentence(55, "I'm extremely glad to see everyone has arrived safely.", "", "Emotions", "Beginner", soundFocus = "gl"),
        DailySentence(56, "I feel so refreshed after taking a short afternoon nap.", "", "Emotions", "Beginner", soundFocus = "f"),
        DailySentence(57, "I was genuinely surprised by the final results of the study.", "", "Emotions", "Intermediate", soundFocus = "g"),
        DailySentence(58, "I am really looking forward to collaborating with your team.", "", "Emotions", "Intermediate", soundFocus = "c"),
        DailySentence(59, "I'm absolutely devastated by the news of the project cancellation.", "", "Emotions", "Advanced", soundFocus = "d"),
        DailySentence(60, "I am deeply moved by the generosity of our supporters.", "", "Emotions", "Advanced", soundFocus = "m"),

        // Household
        DailySentence(61, "Could you please sweep the kitchen floor and take out the trash?", "", "Household", "Beginner", soundFocus = "sw"),
        DailySentence(62, "I need to fold the laundry and put the clothes back in the closet.", "", "Household", "Beginner", soundFocus = "l"),
        DailySentence(63, "Please make sure to turn off the lights when you leave the living room.", "", "Household", "Beginner", soundFocus = "l"),
        DailySentence(64, "We should clean the refrigerator before we go grocery shopping.", "", "Household", "Intermediate", soundFocus = "r"),
        DailySentence(65, "The kitchen sink seems to be clogged; we might need to call a plumber.", "", "Household", "Intermediate", soundFocus = "cl"),
        DailySentence(66, "Please make sure to set the alarm clock before going to sleep.", "", "Household", "Beginner", soundFocus = "al"),

        // Office
        DailySentence(67, "Let's touch base after the morning standup meeting.", "", "Office", "Beginner", soundFocus = "t"),
        DailySentence(68, "Please send me the agenda before the client presentation starts.", "", "Office", "Beginner", soundFocus = "p"),
        DailySentence(69, "We need to collaborate with the design team to finalize the mockup.", "", "Office", "Intermediate", soundFocus = "c"),
        DailySentence(70, "I would appreciate your feedback on the draft proposal by the end of today.", "", "Office", "Intermediate", soundFocus = "f"),
        DailySentence(71, "The quarterly performance indicators show significant growth across all departments.", "", "Office", "Advanced", soundFocus = "q"),
        DailySentence(72, "The team demonstrated exceptional agility in resolving the software bug.", "", "Office", "Advanced", soundFocus = "ag"),

        // School
        DailySentence(73, "Don't forget to submit your science homework before the bell rings.", "", "School", "Beginner", soundFocus = "s"),
        DailySentence(74, "May I borrow your pencil sharpener for a moment, please?", "", "School", "Beginner", soundFocus = "sh"),
        DailySentence(75, "Our class is planning a field trip to the local science museum next Friday.", "", "School", "Intermediate", soundFocus = "f"),
        DailySentence(76, "She was elected as the class representative for the upcoming academic year.", "", "School", "Intermediate", soundFocus = "r"),
        DailySentence(77, "The curriculum emphasizes critical thinking and creative problem-solving skills.", "", "School", "Advanced", soundFocus = "cr"),

        // College
        DailySentence(78, "I am heading to the campus library to study for my final exams.", "", "College", "Beginner", soundFocus = "l"),
        DailySentence(79, "Did you manage to register for the advanced computer science seminar?", "", "College", "Intermediate", soundFocus = "s"),
        DailySentence(80, "He received a full scholarship for his outstanding academic achievements.", "", "College", "Intermediate", soundFocus = "sch"),
        DailySentence(81, "Many undergraduate students participate in peer-reviewed research projects.", "", "College", "Advanced", soundFocus = "p"),
        DailySentence(82, "The professor provided an insightful analysis of post-modern literature.", "", "College", "Advanced", soundFocus = "p"),
        DailySentence(83, "He was admitted to the prestigious academy to pursue classical music.", "", "College", "Advanced", soundFocus = "ac"),

        // Hospital
        DailySentence(84, "Please wait in the reception area until the nurse calls your name.", "", "Hospital", "Beginner", soundFocus = "r"),
        DailySentence(85, "You should take this prescribed medication twice a day after meals.", "", "Hospital", "Beginner", soundFocus = "m"),
        DailySentence(86, "The doctor recommended a comprehensive blood test to monitor your cholesterol.", "", "Hospital", "Intermediate", soundFocus = "d"),
        DailySentence(87, "Please fill out this medical history form before your consultation.", "", "Hospital", "Intermediate", soundFocus = "h"),
        DailySentence(88, "The emergency room staff acted with incredible speed and professionalism.", "", "Hospital", "Advanced", soundFocus = "st"),

        // Police Station
        DailySentence(89, "I came to the station to report a lost wallet.", "", "Police Station", "Beginner", soundFocus = "st"),
        DailySentence(90, "Please describe the suspect's appearance in as much detail as possible.", "", "Police Station", "Intermediate", soundFocus = "d"),
        DailySentence(91, "The officers are investigating the incident and checking the security footage.", "", "Police Station", "Intermediate", soundFocus = "o"),
        DailySentence(92, "You need to file an official complaint regarding the property damage.", "", "Police Station", "Intermediate", soundFocus = "f"),
        DailySentence(93, "The detective gathered crucial evidence to support the ongoing investigation.", "", "Police Station", "Advanced", soundFocus = "d"),

        // Bus Stand
        DailySentence(94, "Which bus goes directly to the city center from here?", "", "Bus Stand", "Beginner", soundFocus = "b"),
        DailySentence(95, "The bus is scheduled to arrive at platform number four in ten minutes.", "", "Bus Stand", "Beginner", soundFocus = "b"),
        DailySentence(96, "I need to purchase a monthly transit pass at the ticket counter.", "", "Bus Stand", "Intermediate", soundFocus = "t"),
        DailySentence(97, "There is a long queue of passengers waiting at the main bus terminal.", "", "Bus Stand", "Intermediate", soundFocus = "q"),
        DailySentence(98, "Commuters are advised to expect minor delays due to road maintenance.", "", "Bus Stand", "Advanced", soundFocus = "c"),

        // Inside Bus
        DailySentence(99, "Is this seat taken, or may I sit here?", "", "Inside Bus", "Beginner", soundFocus = "s"),
        DailySentence(100, "Please move to the back of the bus to let others board.", "", "Inside Bus", "Beginner", soundFocus = "b"),
        DailySentence(101, "You should press the stop button before your destination arrives.", "", "Inside Bus", "Intermediate", soundFocus = "st"),
        DailySentence(102, "Please hold onto the handrail tightly while the bus is in motion.", "", "Inside Bus", "Intermediate", soundFocus = "h"),
        DailySentence(103, "Passengers must validate their transit cards immediately upon boarding.", "", "Inside Bus", "Advanced", soundFocus = "v"),

        // Train Station
        DailySentence(104, "Excuse me, where is the ticket office for regional trains?", "", "Train Station", "Beginner", soundFocus = "t"),
        DailySentence(105, "The express train to London will depart from platform six.", "", "Train Station", "Beginner", soundFocus = "p"),
        DailySentence(106, "Please keep clear of the platform edge as the train approaches.", "", "Train Station", "Intermediate", soundFocus = "pl"),
        DailySentence(107, "I missed the morning commuter train and had to wait for the next one.", "", "Train Station", "Intermediate", soundFocus = "tr"),
        DailySentence(108, "The high-speed rail service offers a seamless connection between major cities.", "", "Train Station", "Advanced", soundFocus = "s"),

        // Inside Train
        DailySentence(109, "Can you help me place this suitcase in the overhead rack?", "", "Inside Train", "Beginner", soundFocus = "s"),
        DailySentence(110, "The conductor is checking tickets, so please have yours ready.", "", "Inside Train", "Intermediate", soundFocus = "c"),
        DailySentence(111, "This quiet carriage is reserved for passengers who wish to rest.", "", "Inside Train", "Intermediate", soundFocus = "q"),
        DailySentence(112, "We can get some hot coffee and sandwiches from the buffet car.", "", "Inside Train", "Intermediate", soundFocus = "b"),
        DailySentence(113, "The train glided smoothly through the countryside, offering picturesque views.", "", "Inside Train", "Advanced", soundFocus = "gl"),

        // Inside Flight
        DailySentence(114, "Please fasten your seatbelt and keep your tray table closed.", "", "Inside Flight", "Beginner", soundFocus = "f"),
        DailySentence(115, "Would you prefer a window seat or an aisle seat?", "", "Inside Flight", "Beginner", soundFocus = "s"),
        DailySentence(116, "The flight attendants are preparing to serve refreshments shortly.", "", "Inside Flight", "Intermediate", soundFocus = "fl"),
        DailySentence(117, "In case of turbulence, please remain seated with your seatbelt fastened.", "", "Inside Flight", "Intermediate", soundFocus = "t"),
        DailySentence(118, "The captain announced that we have reached our cruising altitude of thirty thousand feet.", "", "Inside Flight", "Advanced", soundFocus = "c"),

        // Airport
        DailySentence(119, "We need to check in our luggage before heading to security.", "", "Airport", "Beginner", soundFocus = "l"),
        DailySentence(120, "Where can I find the international departures terminal?", "", "Airport", "Beginner", soundFocus = "d"),
        DailySentence(121, "Please have your boarding pass and passport ready for inspection.", "", "Airport", "Intermediate", soundFocus = "b"),
        DailySentence(122, "The flight has been delayed by two hours due to adverse weather conditions.", "", "Airport", "Intermediate", soundFocus = "w"),
        DailySentence(123, "Duty-free shops are located just beyond the security checkpoints.", "", "Airport", "Advanced", soundFocus = "d"),

        // Inside Ship
        DailySentence(124, "Let's go up to the deck to watch the sunset over the horizon.", "", "Inside Ship", "Beginner", soundFocus = "d"),
        DailySentence(125, "My cabin is located on the second deck near the elevator.", "", "Inside Ship", "Beginner", soundFocus = "c"),
        DailySentence(126, "The cruise liner features several restaurants, a swimming pool, and a theater.", "", "Inside Ship", "Intermediate", soundFocus = "cr"),
        DailySentence(127, "In the event of an emergency, please gather at the designated muster station.", "", "Inside Ship", "Intermediate", soundFocus = "m"),
        DailySentence(128, "The ship navigated effortlessly through the deep waters of the fjord.", "", "Inside Ship", "Advanced", soundFocus = "sh"),
        DailySentence(129, "Emergency personnel responded swiftly to the distress signal from the vessel.", "", "Inside Ship", "Advanced", soundFocus = "em"),

        // Sea Port
        DailySentence(130, "We watched the massive cargo ships load and unload container boxes.", "", "Sea Port", "Beginner", soundFocus = "c"),
        DailySentence(131, "The ferry sails from this harbor twice a day to the neighboring island.", "", "Sea Port", "Beginner", soundFocus = "f"),
        DailySentence(132, "Port authorities are inspecting the incoming vessel for safety compliance.", "", "Sea Port", "Intermediate", soundFocus = "p"),
        DailySentence(133, "The bustling seaport serves as a crucial gateway for international trade.", "", "Sea Port", "Intermediate", soundFocus = "s"),
        DailySentence(134, "Maritime commerce has flourished here for centuries due to the deep-water harbor.", "", "Sea Port", "Advanced", soundFocus = "m"),

        // Government Office
        DailySentence(135, "You need to submit a written application to renew your passport.", "", "Government Office", "Beginner", soundFocus = "p"),
        DailySentence(136, "Please take a queue number and wait for your turn.", "", "Government Office", "Beginner", soundFocus = "q"),
        DailySentence(137, "The administration requires official documentation to verify your current address.", "", "Government Office", "Intermediate", soundFocus = "a"),
        DailySentence(138, "I filled out the necessary paperwork to obtain a building permit.", "", "Government Office", "Intermediate", soundFocus = "p"),
        DailySentence(139, "Bureaucratic procedures often require meticulous attention to detail and multiple approvals.", "", "Government Office", "Advanced", soundFocus = "b"),

        // Politics
        DailySentence(140, "Citizens are encouraged to vote in the upcoming municipal elections.", "", "Politics", "Beginner", soundFocus = "v"),
        DailySentence(141, "The local mayor announced a new plan to build more public parks.", "", "Politics", "Beginner", soundFocus = "m"),
        DailySentence(142, "The candidate's speech focused on economic reform and education funding.", "", "Politics", "Intermediate", soundFocus = "s"),
        DailySentence(143, "Democratic processes rely heavily on transparency and active citizen participation.", "", "Politics", "Advanced", soundFocus = "d"),
        DailySentence(144, "The government policy sparked a heated debate regarding fiscal responsibility.", "", "Politics", "Advanced", soundFocus = "p"),
        DailySentence(145, "Political analysts predict a tight contest in the upcoming parliamentary election.", "", "Politics", "Advanced", soundFocus = "p"),

        // World Wide Travelling
        DailySentence(146, "I love exploring foreign countries and trying authentic local dishes.", "", "World Wide Travelling", "Beginner", soundFocus = "f"),
        DailySentence(147, "Remember to buy travel insurance before you embark on your journey.", "", "World Wide Travelling", "Beginner", soundFocus = "t"),
        DailySentence(148, "Backpacking across Europe is a popular adventure for young travelers.", "", "World Wide Travelling", "Intermediate", soundFocus = "b"),
        DailySentence(149, "Cultural immersion allows you to understand different perspectives and ways of life.", "", "World Wide Travelling", "Intermediate", soundFocus = "c"),
        DailySentence(150, "Globetrotting requires adaptability, open-mindedness, and respect for diverse customs.", "", "World Wide Travelling", "Advanced", soundFocus = "g"),

        // Rural Travelling
        DailySentence(151, "We enjoyed a peaceful weekend walk through the quiet countryside fields.", "", "Rural Travelling", "Beginner", soundFocus = "c"),
        DailySentence(152, "The dirt road was bumpy, but the scenic views were absolutely breathtaking.", "", "Rural Travelling", "Beginner", soundFocus = "r"),
        DailySentence(153, "We stayed in a charming cottage nestled in a remote mountain valley.", "", "Rural Travelling", "Intermediate", soundFocus = "c"),
        DailySentence(154, "Rustic villages often preserve traditional crafts and historical architecture.", "", "Rural Travelling", "Intermediate", soundFocus = "v"),
        DailySentence(155, "The simplicity of agrarian life offers a welcome respite from urban chaos.", "", "Rural Travelling", "Advanced", soundFocus = "s"),

        // Traffics
        DailySentence(156, "We got stuck in a massive traffic jam on the main highway.", "", "Traffics", "Beginner", soundFocus = "t"),
        DailySentence(157, "Always look both ways before crossing the busy street.", "", "Traffics", "Beginner", soundFocus = "s"),
        DailySentence(158, "The city traffic lights are coordinated to improve the flow of vehicles.", "", "Traffics", "Intermediate", soundFocus = "l"),
        DailySentence(159, "During rush hour, commuting by bicycle is often faster than driving.", "", "Traffics", "Intermediate", soundFocus = "r"),
        DailySentence(160, "Gridlock in metropolitan areas causes significant economic losses and environmental pollution.", "", "Traffics", "Advanced", soundFocus = "g"),

        // Business related
        DailySentence(161, "Our company is looking to hire a new marketing manager.", "", "Business related", "Beginner", soundFocus = "m"),
        DailySentence(162, "We need to draft a realistic budget for the next financial year.", "", "Business related", "Beginner", soundFocus = "b"),
        DailySentence(163, "The partnership agreement will expand our presence in the global market.", "", "Business related", "Intermediate", soundFocus = "p"),
        DailySentence(164, "Entrepreneurs must balance risk-taking with careful strategic planning.", "", "Business related", "Intermediate", soundFocus = "e"),
        DailySentence(165, "Diversification of our product portfolio is essential to sustain long-term growth.", "", "Business related", "Advanced", soundFocus = "d"),

        // Grocery shopping
        DailySentence(166, "Please add fresh milk, bread, and eggs to our shopping list.", "", "Grocery shopping", "Beginner", soundFocus = "sh"),
        DailySentence(167, "Is there a discount on these organic apples today?", "", "Grocery shopping", "Beginner", soundFocus = "d"),
        DailySentence(168, "I prefer to buy fresh produce from the local farmers' market.", "", "Grocery shopping", "Intermediate", soundFocus = "f"),
        DailySentence(169, "Please weigh the vegetables before checking out at the register.", "", "Grocery shopping", "Intermediate", soundFocus = "v"),
        DailySentence(170, "Using reusable shopping bags helps reduce plastic waste significantly.", "", "Grocery shopping", "Intermediate", soundFocus = "r"),

        // Cutting
        DailySentence(171, "Be extremely careful when cutting the carrots with that sharp knife.", "", "Cutting", "Beginner", soundFocus = "c"),
        DailySentence(172, "He is going to the barber shop to get a quick haircut.", "", "Cutting", "Beginner", soundFocus = "h"),
        DailySentence(173, "We need to chop the wood into small pieces for the fireplace.", "", "Cutting", "Intermediate", soundFocus = "ch"),
        DailySentence(174, "Please slice the bread thinly and toast it until golden brown.", "", "Cutting", "Intermediate", soundFocus = "sl"),
        DailySentence(175, "The gardener is carefully trimming the overgrown hedges along the fence.", "", "Cutting", "Intermediate", soundFocus = "tr"),

        // Cooking
        DailySentence(176, "I love baking chocolate chip cookies on rainy Sunday afternoons.", "", "Cooking", "Beginner", soundFocus = "b"),
        DailySentence(177, "Please stir the tomato sauce constantly so it doesn't burn.", "", "Cooking", "Beginner", soundFocus = "st"),
        DailySentence(178, "Let the soup simmer on low heat for about twenty minutes.", "", "Cooking", "Intermediate", soundFocus = "s"),
        DailySentence(179, "You should marinate the chicken in lemon and garlic before grilling.", "", "Cooking", "Intermediate", soundFocus = "m"),
        DailySentence(180, "Culinary arts require a perfect balance of scientific precision and artistic expression.", "", "Cooking", "Advanced", soundFocus = "c"),

        // Military related
        DailySentence(181, "The soldiers marched in perfect formation during the parade.", "", "Military related", "Beginner", soundFocus = "s"),
        DailySentence(182, "The troops were deployed to the border for a defense exercise.", "", "Military related", "Intermediate", soundFocus = "t"),
        DailySentence(183, "The general outlined a comprehensive strategic plan to the joint chiefs.", "", "Military related", "Advanced", soundFocus = "g"),
        DailySentence(184, "National defense requires continuous modernization of technological capabilities.", "", "Military related", "Advanced", soundFocus = "d"),
        DailySentence(185, "The peace treaty marked the official end of hostilities between the nations.", "", "Military related", "Advanced", soundFocus = "p"),

        // Family relations related
        DailySentence(186, "My grandparents love telling stories about their childhood days.", "", "Family relations related", "Beginner", soundFocus = "g"),
        DailySentence(187, "We always gather at my parents' house for Sunday dinner.", "", "Family relations related", "Beginner", soundFocus = "p"),
        DailySentence(188, "Siblings often share a unique bond built on shared childhood experiences.", "", "Family relations related", "Intermediate", soundFocus = "s"),
        DailySentence(189, "Maintaining close family ties is essential for emotional well-being.", "", "Family relations related", "Intermediate", soundFocus = "f"),
        DailySentence(190, "Nurturing relationships across generations enriches our understanding of family history.", "", "Family relations related", "Advanced", soundFocus = "g"),

        // Family functions related
        DailySentence(191, "Our family is hosting a big reunion party next month.", "", "Family functions related", "Beginner", soundFocus = "r"),
        DailySentence(192, "They are planning a beautiful outdoor wedding in the spring.", "", "Family functions related", "Beginner", soundFocus = "w"),
        DailySentence(193, "We celebrated my grandfather's eightieth birthday with a surprise dinner.", "", "Family functions related", "Intermediate", soundFocus = "b"),
        DailySentence(194, "The golden wedding anniversary was attended by family and close friends.", "", "Family functions related", "Intermediate", soundFocus = "g"),
        DailySentence(195, "Milestone celebrations provide an excellent opportunity to strengthen familial connections.", "", "Family functions related", "Advanced", soundFocus = "m"),

        // Festival related
        DailySentence(196, "We decorated our entire house with colorful lights for the festival.", "", "Festival related", "Beginner", soundFocus = "l"),
        DailySentence(197, "Everyone is wearing traditional clothes to celebrate the holiday.", "", "Festival related", "Beginner", soundFocus = "t"),
        DailySentence(198, "The annual carnival features lively music, delicious food, and street parades.", "", "Festival related", "Intermediate", soundFocus = "c"),
        DailySentence(199, "Festivals serve as a vibrant expression of cultural heritage and community unity.", "", "Festival related", "Intermediate", soundFocus = "f"),
        DailySentence(200, "The spectacular fireworks display lit up the night sky during the celebration.", "", "Festival related", "Advanced", soundFocus = "f")
    )

    val paragraphs = listOf(
        ParagraphSet(
            id = 1,
            title = "The Power of Habit",
            text = "Most of the choices we make each day may feel like the products of well-considered decision-making, but they are not. They are habits. While each habit means relatively little on its own, over time, the meals we order, what we say to our kids, and how we spend our money have enormous impacts on our health, productivity, financial security, and happiness.",
            level = "Beginner",
            estimatedReadingTime = 40
        ),
        ParagraphSet(
            id = 2,
            title = "Our Blue Planet",
            text = "Water is the lifeblood of our planet. Covering over seventy percent of the Earth's surface, the oceans regulate global climate, provide food for billions, and produce more than half of the oxygen we breathe. Protecting these vast marine ecosystems is not just an environmental luxury; it is critical for human survival and global prosperity.",
            level = "Intermediate",
            estimatedReadingTime = 45
        ),
        ParagraphSet(
            id = 3,
            title = "A Curious Traveler",
            text = "Traveling to unfamiliar places allows us to view the world through a completely fresh lens. It challenges our comfortable assumptions, sparks creativity, and invites us to practice empathy with strangers. When we step outside of our daily routines, we begin to discover that despite our diverse cultures and traditions, we share far more similarities than differences.",
            level = "Intermediate",
            estimatedReadingTime = 50
        ),
        ParagraphSet(
            id = 4,
            title = "The Future of Artificial Intelligence",
            text = "The exponential rise of neural computing is driving unprecedented changes across global industries. Beyond simple automation, these systems are now augmenting human capabilities in diagnosing complex diseases, predicting environmental disasters, and composing beautiful art. Embracing this shift requires not just technical skill, but a strong moral compass.",
            level = "Advanced",
            estimatedReadingTime = 60
        )
    )

    val tongueTwisters = listOf(
        TongueTwister(1, "b", "A big black bug bit a big black bear.", "Focus on the strong explosive 'b' sound with your lips.", "Easy"),
        TongueTwister(2, "b vs d", "Good blood, bad blood, cold blood, hot blood.", "Focus on the vowel pronunciation and quick ending plosives.", "Medium"),
        TongueTwister(3, "b vs p", "Peter Piper picked a peck of pickled peppers.", "Focus on the sudden burst of air for the 'p' sounds compared to the softer 'b' sound.", "Hard"),
        TongueTwister(4, "b vs t", "Betty Botter bought some butter but she said the butter's bitter.", "Focus on the soft flap-t and initial explosive 'b' sounds.", "Medium"),
        TongueTwister(5, "c vs k", "Cooks cook cupcakes quickly, cooks cook cupcakes quickly.", "Focus on the crisp, sharp voiceless velar plosive 'k' sound.", "Easy"),
        TongueTwister(6, "ch vs sh", "Chester cheetah chews cheap cheddar cheese.", "Focus on the difference between the stop consonant 'ch' and the continuous 'sh'.", "Medium"),
        TongueTwister(7, "ch vs sh", "If a dog chews shoes, whose shoes does he choose?", "Focus on the distinction between 'ch' and 'sh' sounds.", "Easy"),
        TongueTwister(8, "ck", "Six sick slick slim sycamore saplings.", "Focus on the crisp 's' and 'k' consonants.", "Hard"),
        TongueTwister(9, "cl vs cr", "How can a clam cram in a clean cream can?", "Focus on the 'cl' and 'cr' consonant blends.", "Medium"),
        TongueTwister(10, "f", "Four furious friends fought for the phone.", "Focus on the soft friction sound of placing your upper teeth on your lower lip.", "Easy"),
        TongueTwister(11, "f vs th", "Fred fed Ted bread, and Ted fed Fred bread.", "Focus on vowel consistency and rapid consonant transitions.", "Hard"),
        TongueTwister(12, "f vs z", "Fuzzy Wuzzy was a bear, Fuzzy Wuzzy had no hair.", "Focus on the rapid 'f' and buzzing 'z' sounds.", "Easy"),
        TongueTwister(13, "n", "Nine nice night nurses nursing nicely.", "Focus on the alveolar nasal 'n' sound coupled with differing vowels.", "Easy"),
        TongueTwister(14, "p", "Proper copper coffee pot, proper copper coffee pot.", "Focus on the crisp pop of 'p' and the back-of-throat 'c' and 'g' transitions.", "Medium"),
        TongueTwister(15, "r vs l", "Red lorry, yellow lorry, red lorry, yellow lorry.", "Focus on the transition between 'r' and 'l'. Don't let them blur together!", "Medium"),
        TongueTwister(16, "r vs l", "Truly rural, truly rural, purely plural.", "Extremely challenging transition between the liquid consonants 'r' and 'l'.", "Hard"),
        TongueTwister(17, "r vs l", "A loyal warrior will rarely worry.", "Focus on the fluid transition of liquid consonants 'l' and 'r' with the semi-vowel 'w'.", "Hard"),
        TongueTwister(18, "r vs w", "Rory the warrior and Roger the worrier were reared wrongly.", "Extremely difficult back-to-back liquid 'r' and 'w' sound integration.", "Hard"),
        TongueTwister(19, "s vs sh", "She sells seashells by the seashore.", "Focus on switching quickly between the sharp 's' and soft 'sh' sounds.", "Easy"),
        TongueTwister(20, "s vs sh", "Susie works in a shoeshine shop where she shines and she sits.", "Focus on the challenging alternation between 's' and 'sh'.", "Medium"),
        TongueTwister(21, "sh", "Selfish shellfish cellars shall sell seafood.", "Focus on the quick shift between 's' and 'sh'.", "Medium"),
        TongueTwister(22, "st vs k", "Six sticky skeletons, six sticky skeletons.", "Focus on the consonant clusters 'st' and 'sk' in close succession.", "Medium"),
        TongueTwister(23, "th", "Three thin thieves thought a thousand thrilling thoughts.", "Focus on the voiceless 'th' sound. Keep your tongue slightly between your front teeth.", "Easy"),
        TongueTwister(24, "th", "Whether the weather is cold, or whether the weather is hot.", "Focus on the voiced 'th' sound, which requires vocal cord vibration.", "Easy"),
        TongueTwister(25, "th", "The thirty-three thieves thought they thrilled the throne.", "Focus on both voiced and voiceless 'th' sounds.", "Hard"),
        TongueTwister(26, "th", "I thought a thought, but the thought I thought wasn't the thought I thought.", "Focus on the speed and clarity of the voiceless dental fricative 'th'.", "Hard"),
        TongueTwister(27, "thr vs fr", "He threw three free throws, he threw three free throws.", "Focus on the tight differentiation between 'thr' and 'fr' clusters.", "Medium"),
        TongueTwister(28, "v vs w", "Very well, Victor volunteered to wash the velvety vest.", "Focus on biting your bottom lip for the 'v' sound, and rounding your lips for the 'w' sound.", "Medium"),
        TongueTwister(29, "w", "Which witch wished which wicked wish?", "Focus on rounding your lips for the 'w' sound.", "Easy"),
        TongueTwister(30, "z", "Zippy zebra zipped past the busy buzzing bazaar.", "Focus on the buzzing vibration of the vocal cords for the 'z' sound.", "Medium")
    )
}
