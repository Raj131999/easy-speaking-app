const fs = require('fs');
const path = 'app/src/main/assets/useful_lessons.json';
const lessons = JSON.parse(fs.readFileSync(path, 'utf8'));

function fmt(items) {
    return items.map(item => `• ${item.title}: ${item.text}`).join('\n');
}

const customMap = {};

// Lessons 1 - 10: Modal Verbs
customMap[1] = fmt([
    { title: "Boss / Workplace Instruction (have to)", text: "My manager told me I have to finish this presentation before tomorrow's meeting." },
    { title: "Company Policy (have to)", text: "All employees have to submit their weekly timesheets by Friday evening." },
    { title: "Law / Official Regulation (have to)", text: "Drivers have to stop when the traffic light turns red." },
    { title: "Self-Imposed Duty (must)", text: "I must call my mother tonight because I miss her." },
    { title: "Conscious / Internal Duty (must)", text: "I feel I must buy fresh vegetables and cook a healthy meal tonight." }
]);

customMap[2] = fmt([
    { title: "Affirmative Obligation (have to)", text: "You have to wear a seat belt when driving on the highway." },
    { title: "Negative Prohibition (must not / mustn't)", text: "You must not leave your homework incomplete." },
    { title: "Lack of Necessity (don't have to)", text: "You don't have to wear a tie to work on Casual Friday." },
    { title: "Past Obligation (had to)", text: "Yesterday, I had to work late to complete the quarterly audit." },
    { title: "Future Obligation (will have to)", text: "Next week, we will have to submit our project proposals." }
]);

customMap[3] = fmt([
    { title: "No Choice (have to / must)", text: "I have to submit the financial reports tomorrow morning." },
    { title: "Expected Rule with Choice (supposed to)", text: "We are supposed to submit the reports by tomorrow, but we can request an extension." },
    { title: "Advice / Suggestion (ought to)", text: "You ought to get some sleep before your early flight." }
]);

customMap[4] = fmt([
    { title: "Obligation (have to)", text: "I have to pay my property taxes by the end of the month." },
    { title: "Expected Behavior (supposed to)", text: "You are supposed to keep your phone on silent during the presentation." },
    { title: "Moral Duty / Suggestion (ought to)", text: "We ought to help our elderly neighbors carry their heavy groceries." },
    { title: "Negative Expected Rule (not supposed to)", text: "You are not supposed to talk loudly inside a hospital." }
]);

customMap[5] = fmt([
    { title: "Personal Obligation", text: "I must finish reading this textbook before my exam on Monday." },
    { title: "Logical Certainty", text: "He has been working in the garden all afternoon, so he must be tired." },
    { title: "Strong Recommendation", text: "You must visit that new Italian restaurant downtown—the food is incredible." },
    { title: "Question Form (Do you have to?)", text: "Do you have to leave so early, or can you stay for coffee?" }
]);

customMap[6] = fmt([
    { title: "Prohibition / Restriction (mustn't)", text: "You mustn't tell anyone our secret company plans." },
    { title: "Lack of Necessity / Optional (needn't)", text: "You needn't carry physical documents with you since everything is saved online." }
]);

customMap[7] = fmt([
    { title: "Main Verb (need to)", text: "I need to buy some groceries for dinner tonight." },
    { title: "Modal Verb (needn't)", text: "You needn't worry about dinner reservations; I already booked a table." },
    { title: "Noun Form (in need of)", text: "The local shelter is in urgent need of warm blankets." }
]);

customMap[8] = fmt([
    { title: "Giving Advice", text: "You should see a doctor if your fever doesn't go down." },
    { title: "Expecting Probability", text: "The train should arrive in about ten minutes." },
    { title: "Negative Advice (shouldn't)", text: "You shouldn't drink coffee right before going to bed." },
    { title: "Past Regret (should have)", text: "I should have studied harder for the final exam." }
]);

customMap[9] = fmt([
    { title: "Recommendation", text: "You should try the roasted salmon at this café." },
    { title: "Logical Expectation", text: "They left an hour ago, so they should be here soon." },
    { title: "Criticism / Past Regret (should have)", text: "You should have warned me about the heavy traffic." }
]);

customMap[10] = fmt([
    { title: "Future Expectation (should)", text: "He should reach the office any minute now." },
    { title: "Negative Suggestion (shouldn't)", text: "You shouldn't stay up late watching television." },
    { title: "Past Regret (should have)", text: "I shouldn't have shouted at you during the meeting yesterday." }
]);

// Lessons 11 - 15: Could, Can, Will, Would
customMap[11] = fmt([
    { title: "Past Ability (could)", text: "When I was younger, I could run ten miles without stopping." },
    { title: "Present Possibility (could)", text: "It could rain later this afternoon, so take an umbrella." },
    { title: "Polite Request (Could you...)", text: "Could you please pass me the water pitcher?" },
    { title: "Past Possibility / Missed Opportunity (could have)", text: "I could have won the race, but I tripped near the finish line." }
]);

customMap[12] = fmt([
    { title: "Present Possibility", text: "We could eat dinner outdoors if the weather stays warm." },
    { title: "Past General Ability", text: "In high school, she could play the piano effortlessly." },
    { title: "Future Suggestion", text: "We could go on a road trip this upcoming weekend." },
    { title: "Conditional Sentence", text: "We could travel the world if we won the lottery." }
]);

customMap[13] = fmt([
    { title: "Present Ability (can)", text: "He can speak three languages fluently." },
    { title: "Past Ability (could)", text: "She could swim across the lake when she was ten." },
    { title: "Informal Permission (can)", text: "Can I use your phone for a quick call?" },
    { title: "Polite Request (could)", text: "Could I borrow your book for a day, please?" }
]);

customMap[14] = fmt([
    { title: "Certain Future / Promise (will)", text: "I will pick you up at six o'clock sharp." },
    { title: "Hypothetical Situation (would)", text: "I would pick you up if I had a car available." },
    { title: "Past Habitual Action (would)", text: "Every summer, we would visit our grandparents in the countryside." },
    { title: "Polite Offer / Request (would)", text: "Would you like a cup of hot tea?" }
]);

customMap[15] = fmt([
    { title: "Conditional Statement", text: "If I had more free time, I would study photography." },
    { title: "Polite Request (Would you mind...)", text: "Would you mind closing the window, please?" },
    { title: "Preferences (would rather)", text: "I would rather stay home than go to a crowded party." },
    { title: "Past Habit", text: "Grandfather would tell us stories by the fireplace every evening." }
]);

// Lessons 16 - 20: Shall, Will, May, Might
customMap[16] = fmt([
    { title: "Offering Help (Shall I...)", text: "Shall I carry those heavy bags to your car?" },
    { title: "Initiating Action (Shall we...)", text: "Shall we begin the presentation now?" },
    { title: "Future Prediction / Outcome (Will we...)", text: "Will we arrive at the airport on time?" }
]);

customMap[17] = fmt([
    { title: "Offer / Suggestion (Shall I / Shall we)", text: "Shall I turn on the air conditioner?" },
    { title: "Future Event Inquiry (Will I / Will we)", text: "Will we get a certificate after completing the course?" }
]);

customMap[18] = fmt([
    { title: "Past Habitual Action", text: "I would read a lot during my college days." },
    { title: "Indirect Speech Conversion", text: "He promised that he would send the email." },
    { title: "Preference (would rather)", text: "I would rather drink tea than coffee." }
]);

customMap[19] = fmt([
    { title: "Spontaneous Decision", text: "It's cold; I will close the window." },
    { title: "Future Prediction with Belief", text: "The sun will rise over there tomorrow morning." },
    { title: "Promise / Commitment", text: "I will always support your dreams." }
]);

customMap[20] = fmt([
    { title: "Formal Permission (may)", text: "May I leave the conference room early today?" },
    { title: "High Possibility (may)", text: "The store may close early because of the storm." },
    { title: "Lower / Slight Possibility (might)", text: "I might visit my aunt this weekend if I finish my work." }
]);

// Lessons 21 - 25: Past Modal Combinations
customMap[21] = fmt([
    { title: "Around 50% Certainty (may have)", text: "She may have gone to the central library." },
    { title: "Slight Possibility (might have)", text: "I might have left my keys in the office, but I am not sure." },
    { title: "High Logical Certainty (must have)", text: "He must have left his phone at home because he isn't answering." }
]);

customMap[22] = fmt([
    { title: "Formal Possibility (may have)", text: "The company may have experienced financial delays." },
    { title: "Spoken Possibility (might have)", text: "We might have taken the wrong turn at the intersection." },
    { title: "Deduction / Strong Evidence (must have)", text: "The ground is soaked; it must have rained heavily last night." }
]);

customMap[23] = fmt([
    { title: "Missed Possibility (could have)", text: "We could have won the match if we had practiced more." },
    { title: "Past Regret / Obligation (should have)", text: "I should have double-checked the flight times before leaving." },
    { title: "Imaginary Past Outcome (would have)", text: "I would have attended the wedding, but I fell ill." },
    { title: "Slight Past Guess (might have)", text: "They might have forgotten about our dinner meeting." },
    { title: "Strong Logical Conclusion (must have)", text: "She must have passed the test easily because she studied for weeks." }
]);

customMap[24] = fmt([
    { title: "Missed Obligation / Regret (should have done)", text: "You should have done your homework before playing games." },
    { title: "Untaken Opportunity / Choice (could have done)", text: "We could have done the tour by bike, but we chose to walk." },
    { title: "Blocked Intention (would have done)", text: "I would have done the cooking, but we ran out of gas." }
]);

customMap[25] = fmt([
    { title: "Past Participle Experience (been)", text: "I have been to London twice in the past three years." },
    { title: "Temporary Behavior (being)", text: "You are being overly dramatic about a minor issue." },
    { title: "Passive Continuous Action (being)", text: "The broken car is being repaired at the mechanic shop." }
]);

// Lessons 26 - 31
customMap[26] = fmt([
    { title: "Temporary Behavior", text: "Why are you being so quiet this morning?" },
    { title: "Passive Voice Action", text: "The road is currently being resurfaced." },
    { title: "Gerund / Noun Role", text: "I truly enjoy being a mentor to young students." }
]);

customMap[27] = fmt([
    { title: "Travel & Life Experiences", text: "Have you ever been to Hawaii?" },
    { title: "State / Condition Over Time", text: "She has been ill for three days." },
    { title: "Ongoing Duration in Present Perfect", text: "We have been waiting for the bus since eight o'clock." }
]);

customMap[28] = fmt([
    { title: "Temporary Behavior + Adjective", text: "He is being foolish by ignoring the doctor's advice." },
    { title: "Passive Action in Progress", text: "The new building is being constructed downtown." },
    { title: "Cause / Reason Replacement", text: "Being tired after the long drive, she went straight to bed." }
]);

customMap[29] = fmt([
    { title: "Permanent Possession / State (have)", text: "They have a lovely summer home near the ocean." },
    { title: "Ongoing Temporary Experience (having)", text: "We are having lunch at a sidewalk terrace." },
    { title: "Events & Meetings (having)", text: "Our company is having an annual meeting next Tuesday." }
]);

customMap[30] = fmt([
    { title: "Had Had (Auxiliary Had + Past Participle Had)", text: "By the time the guests arrived, we had already had our dinner." },
    { title: "Past Perfect Experience", text: "She felt confident because she had had extensive training prior to the test." }
]);

customMap[31] = fmt([
    { title: "Present Perfect Continuous Possession (have had)", text: "I have had this car for over ten years." },
    { title: "Recent Completed Experience", text: "We have had a wonderful afternoon spending time together." }
]);

// Lessons 32 - 36
customMap[32] = fmt([
    { title: "Question with I / You / We / They (do)", text: "Do you enjoy listening to classical music?" },
    { title: "Question with He / She / It (does)", text: "Does she live near the city center?" },
    { title: "Negative Statement (don't / doesn't)", text: "He doesn't eat spicy food." }
]);

customMap[33] = fmt([
    { title: "Past Simple Question (did)", text: "Did you visit your grandparents over the weekend?" },
    { title: "Past Simple Negative (didn't + base verb)", text: "They didn't finish the project on time." }
]);

customMap[34] = fmt([
    { title: "Present Perfect with I/You/We/They (have + V3)", text: "We have already submitted the revised proposal." },
    { title: "Present Perfect with He/She/It (has + V3)", text: "She has completed her master's thesis." }
]);

customMap[35] = fmt([
    { title: "Present Continuous (am/is/are + -ing)", text: "They are discussing the new strategy in the room." },
    { title: "Past Continuous (was/were + -ing)", text: "I was reading a book when the electricity went out." }
]);

customMap[36] = fmt([
    { title: "Emphatic Assertion (do)", text: "I do care deeply about your future success!" },
    { title: "Emphatic Assertion (does)", text: "She does work hard, even if she stays quiet." },
    { title: "Emphatic Assertion in Past (did)", text: "We did lock the door before leaving the house." }
]);

// Lessons 37 - 44: Perfect Tenses
customMap[37] = fmt([
    { title: "Life Experience", text: "I have visited France three times in my life." },
    { title: "Recent Accomplishment / Action", text: "He has just finished painting the dining room." }
]);

customMap[38] = fmt([
    { title: "Ongoing Unfinished Action", text: "She has been studying medicine for four years." },
    { title: "Recent Process with Present Effect", text: "It has been raining all morning, so the ground is wet." }
]);

customMap[39] = fmt([
    { title: "Focus on Completion / Quantity (Simple)", text: "I have written three client reports today." },
    { title: "Focus on Duration / Process (Continuous)", text: "I have been writing client reports all morning." }
]);

customMap[40] = fmt([
    { title: "Earlier Past Action (Past Perfect)", text: "When we arrived at the cinema, the movie had already started." },
    { title: "Sequence of Past Events", text: "She had saved enough money before she bought her first apartment." }
]);

customMap[41] = fmt([
    { title: "Ongoing Action Before Past Moment", text: "They had been driving for five hours before they stopped for gasoline." },
    { title: "Past Duration Emphasis", text: "He was exhausted because he had been working out in the sun." }
]);

customMap[42] = fmt([
    { title: "Action Completed Before Future Point", text: "By next month, I will have completed my training course." },
    { title: "Future Deadline", text: "They will have arrived in Tokyo by tomorrow morning." }
]);

customMap[43] = fmt([
    { title: "Projected Future Duration", text: "By December, she will have been living in New York for ten years." },
    { title: "Continuous Progress Up To Future Point", text: "By 5 PM, we will have been negotiating for eight straight hours." }
]);

customMap[44] = fmt([
    { title: "Accurate Sequence in Storytelling", text: "I had been waiting for twenty minutes when she finally arrived." },
    { title: "Expressing Time Relationships", text: "By the time you read this letter, I will have landed in London." }
]);

// Lessons 45 - 53
customMap[45] = fmt([
    { title: "Daily Habit / Routine", text: "I drink green tea every morning after waking up." },
    { title: "Universal Truth / Fact", text: "Water boils at 100 degrees Celsius." },
    { title: "Third-Person Singular (-s / -es)", text: "She works as a financial analyst in a regional bank." }
]);

customMap[46] = fmt([
    { title: "Action Happening Right Now", text: "Please be quiet; the baby is sleeping in the bedroom." },
    { title: "Temporary Current Situation", text: "He is staying at a hotel while his house is renovated." }
]);

customMap[47] = fmt([
    { title: "Finished Action at Specific Past Time", text: "We graduated from university in June 2020." },
    { title: "Irregular Past Forms", text: "She wrote a brilliant essay and sent it to the editor." }
]);

customMap[48] = fmt([
    { title: "Ongoing Background Past Action", text: "At 8 PM yesterday, we were having dinner together." },
    { title: "Setting a Narrative Scene", text: "The sun was setting and birds were singing in the trees." }
]);

customMap[49] = fmt([
    { title: "Background Action (Past Continuous)", text: "While I was studying in my room..." },
    { title: "Short Interrupting Action (Simple Past)", text: "...the phone rang loudly on the desk." }
]);

customMap[50] = fmt([
    { title: "State of Mind / Emotion (love, know, want)", text: "I understand the grammar lesson completely now." },
    { title: "Possession / Relationship (belong, own)", text: "This antique watch belongs to my grandfather." }
]);

customMap[51] = fmt([
    { title: "Opinion / Stative (think)", text: "I think this is a fantastic opportunity for all of us." },
    { title: "Mental Activity / Dynamic (thinking)", text: "I am thinking about moving to a new neighborhood." },
    { title: "Possession / Stative (have)", text: "She has a beautiful garden behind her cottage." },
    { title: "Action / Experience / Dynamic (having)", text: "We are having a great time at the beach." }
]);

customMap[52] = fmt([
    { title: "Spontaneous Decision / Prediction (will)", text: "I am thirsty; I will buy a bottle of water." },
    { title: "Prior Plan / Evidence (be going to)", text: "Look at those dark clouds; it is going to rain." },
    { title: "Fixed Schedule / Arrangement (Present Continuous)", text: "I am meeting the dentist tomorrow at 10 AM." }
]);

customMap[53] = fmt([
    { title: "Future Time Clause with 'When'", text: "When he arrives at the station, we will pick him up." },
    { title: "Future Time Clause with 'As soon as'", text: "As soon as I finish the report, I will send it to you." },
    { title: "Future Time Clause with 'If'", text: "If it rains tomorrow, we will move the party indoors." }
]);

// Lessons 54 - 60
customMap[54] = fmt([
    { title: "Wh- Question Structure (Wh- + Aux + Subject + Verb)", text: "Where do you work during the summer?" },
    { title: "Wh- Question in Past Tense", text: "Why did you call the customer service desk?" }
]);

customMap[55] = fmt([
    { title: "Subject Question (No auxiliary 'do/did')", text: "Who broke the glass vase on the table?" },
    { title: "Object Question (With auxiliary 'do/did')", text: "Who did you invite to the birthday party?" }
]);

customMap[56] = fmt([
    { title: "Positive Statement + Negative Tag", text: "You live in Chicago, don't you?" },
    { title: "Negative Statement + Positive Tag", text: "She isn't coming to the conference, is she?" }
]);

customMap[57] = fmt([
    { title: "Direct Question", text: "Where is the post office?" },
    { title: "Polite Indirect Question (Statement Word Order)", text: "Could you please tell me where the post office is?" }
]);

customMap[58] = fmt([
    { title: "Zero Conditional (General Fact / Scientific Truth)", text: "If you heat ice, it melts into water." },
    { title: "First Conditional (Real Future Possibility)", text: "If I pass the final exam, I will celebrate with my family." }
]);

customMap[59] = fmt([
    { title: "Hypothetical Present / Unreal Situation", text: "If I won a million dollars, I would buy a house on the coast." },
    { title: "Giving Friendly Advice (If I were you...)", text: "If I were you, I would take that job offer immediately." }
]);

customMap[60] = fmt([
    { title: "Unreal Past Event & Regret", text: "If I had set my alarm clock, I would not have missed the flight." },
    { title: "Imaginary Past Outcome", text: "If we had left earlier, we would have avoided the traffic jam." }
]);

// Lessons 61 - 67
customMap[61] = fmt([
    { title: "For (reason / cause)", text: "He bought a warm jacket, for the mountain air was chilly." },
    { title: "And (addition)", text: "I like drinking tea, and she prefers coffee." },
    { title: "Nor (negative choice)", text: "He doesn't eat meat, nor does he drink dairy milk." },
    { title: "But (contrast)", text: "I wanted to go for a walk, but it began to rain heavily." },
    { title: "Or (alternative)", text: "You can take the bus, or you can order a taxi." },
    { title: "Yet (unexpected contrast)", text: "He was completely exhausted, yet he finished the marathon." },
    { title: "So (result / consequence)", text: "It was getting dark, so we decided to return home." }
]);

customMap[62] = fmt([
    { title: "because (direct cause)", text: "We stayed indoors because the weather was stormy." },
    { title: "since (known reason / cause)", text: "Since it was a public holiday, all banks were closed." },
    { title: "as (formal cause)", text: "As the meeting room was full, we moved to the auditorium." },
    { title: "due to the fact that (formal written reason)", text: "Due to the fact that flights were canceled, we stayed an extra night." }
]);

customMap[63] = fmt([
    { title: "both...and", text: "She is both intelligent and hard-working." },
    { title: "either...or", text: "You can either choose the red coat or the blue jacket." },
    { title: "neither...nor", text: "Neither the manager nor the assistant was available for comment." },
    { title: "not only...but also", text: "Not only did he win the prize, but he also set a new national record." }
]);

customMap[64] = fmt([
    { title: "Although / Even though (+ full clause)", text: "Although it was freezing outside, we went for a walk in the park." },
    { title: "Despite / In spite of (+ noun phrase or gerund)", text: "Despite the heavy rain, they arrived at the concert on time." }
]);

customMap[65] = fmt([
    { title: "so that (+ subject + modal verb)", text: "She saved money every month so that she could buy a laptop." },
    { title: "in order to (+ base verb)", text: "He woke up at 5 AM in order to catch the first train." }
]);

customMap[66] = fmt([
    { title: "However (contrast between sentences)", text: "The exam was very difficult; however, most students passed." },
    { title: "Furthermore (adding important information)", text: "She is very talented; furthermore, she works extremely hard." },
    { title: "Therefore (stating a logical result)", text: "The main road was blocked; therefore, we had to take a detour." }
]);

customMap[67] = fmt([
    { title: "whereas (showing clear contrast)", text: "I enjoy reading books, whereas my brother prefers playing video games." },
    { title: "although (subordinating concession)", text: "Although I was exhausted, I finished the project before midnight." }
]);

// Lessons 68 - 86: Prepositions
customMap[68] = fmt([
    { title: "at (exact time / specific moment)", text: "The meeting starts at 9:30 AM sharply." },
    { title: "on (days and dates)", text: "We have an important client presentation on Monday." },
    { title: "in (months, years, seasons, time of day)", text: "My birthday is in September, and we usually travel in summer." }
]);

customMap[69] = fmt([
    { title: "at (specific point / location)", text: "She is standing at the bus stop near the bakery." },
    { title: "on (surfaces)", text: "There is a stack of reports on the desk." },
    { title: "in (enclosed space / city / country)", text: "He works in an office building in downtown Chicago." }
]);

customMap[70] = fmt([
    { title: "into (entering an enclosed space)", text: "He walked into the conference room and sat down." },
    { title: "onto (moving to a surface)", text: "The cat jumped onto the kitchen table." },
    { title: "through (passing inside 3D space)", text: "The train sped through the mountain tunnel." },
    { title: "across (moving side to side)", text: "The boat sailed smoothly across the calm lake." }
]);

customMap[71] = fmt([
    { title: "good at (skill / talent)", text: "She is remarkably good at solving complex puzzles." },
    { title: "interested in (curiosity / hobby)", text: "He is deeply interested in learning ancient history." },
    { title: "afraid of (fear / apprehension)", text: "My younger brother is afraid of dark places." },
    { title: "famous for (reputation / notoriety)", text: "The city of Paris is famous for its art and architecture." }
]);

customMap[72] = fmt([
    { title: "depend on (rely upon)", text: "Our weekend picnic plans depend on the weather forecast." },
    { title: "belong to (possession)", text: "Does this leather briefcase belong to you?" },
    { title: "apologize for (expressing regret)", text: "I sincerely apologize for the delay in answering your email." },
    { title: "listen to (paying attention)", text: "She likes to listen to calming jazz music while working." }
]);

customMap[73] = fmt([
    { title: "In fact (reality / truth)", text: "In fact, we finished the project two days before the deadline." },
    { title: "In advance (ahead of time)", text: "Please book your train tickets well in advance." },
    { title: "In charge of (responsibility)", text: "She is in charge of marketing for the entire region." },
    { title: "In time (early enough)", text: "We arrived at the theater just in time for the opening act." }
]);

customMap[74] = fmt([
    { title: "At night (time of day)", text: "The city street lights turn on automatically at night." },
    { title: "At first (initial stage)", text: "At first, the software was difficult to use, but now it is easy." },
    { title: "At least (minimum amount)", text: "You should drink at least eight glasses of water daily." },
    { title: "At risk (vulnerable / dangerous)", text: "Wild animal habitats are at risk due to deforestation." }
]);

customMap[75] = fmt([
    { title: "On Time (punctual / on schedule)", text: "He showed up right on time for the job interview." },
    { title: "On Purpose (intentionally / deliberately)", text: "I am certain he didn't break the coffee mug on purpose." },
    { title: "On Duty (working / active shift)", text: "Officer Miller is on duty until midnight tonight." },
    { title: "On Average (statistically / typically)", text: "On average, employees spend two hours a day answering emails." }
]);

customMap[76] = fmt([
    { title: "by (modes of transit)", text: "We traveled to Chicago by train." },
    { title: "by (accidental causes)", text: "I deleted the file by mistake." },
    { title: "on foot (exception for walking)", text: "She goes to work on foot every morning." }
]);

customMap[77] = fmt([
    { title: "under pressure (stressful environment)", text: "The team performed exceptionally well under pressure." },
    { title: "under construction (being built / repaired)", text: "The main highway bridge is currently under construction." },
    { title: "over the weekend (duration during weekend)", text: "We plan to repaint the living room over the weekend." },
    { title: "over time (gradually across time)", text: "Your language confidence will improve naturally over time." }
]);

customMap[78] = fmt([
    { title: "At the end (final point of tangible period/thing)", text: "There was a plot twist at the end of the movie." },
    { title: "In the end (finally / eventually after a process)", text: "In the end, we decided to sign the contract." }
]);

customMap[79] = fmt([
    { title: "On time (punctual according to schedule)", text: "The train departed on time at 8:00 AM." },
    { title: "In time (early enough before a deadline/event)", text: "We arrived in time to grab seats before the show started." }
]);

customMap[80] = fmt([
    { title: "reason for (cause / justification)", text: "What was the main reason for the sudden change in plans?" },
    { title: "solution to (answer to a problem)", text: "Scientists are searching for an effective solution to climate change." },
    { title: "advantage of (positive aspect)", text: "One great advantage of living downtown is easy public transport." },
    { title: "relationship with (connection between people)", text: "She maintains a strong professional relationship with her clients." }
]);

customMap[81] = fmt([
    { title: "because of (+ noun phrase)", text: "The match was postponed because of heavy downpour." },
    { title: "due to (often after linking verb 'is/was')", text: "The flight delay was due to severe mechanical issues." }
]);

customMap[82] = fmt([
    { title: "in (private vehicles / small transport)", text: "They drove to the restaurant in a cozy private car." },
    { title: "on (public / large transport)", text: "We met several tourists while sitting on the cross-country train." }
]);

customMap[83] = fmt([
    { title: "Correct: discuss (NO 'about')", text: "We need to discuss the new budget proposal." },
    { title: "Correct: enter (NO 'into' for rooms)", text: "She entered the boardroom quietly." },
    { title: "Correct: answer (NO 'to')", text: "Please answer my question directly." }
]);

customMap[84] = fmt([
    { title: "Besides (in addition to)", text: "Besides managing the sales department, she oversees customer support." },
    { title: "Except for / Apart from (excluding)", text: "Everyone attended the meeting except for the lead developer." }
]);

customMap[85] = fmt([
    { title: "According to (citing a source/authority)", text: "According to the weather report, it will snow tomorrow." },
    { title: "With regard to (introducing a formal topic)", text: "I am writing with regard to your job application submitted yesterday." }
]);

customMap[86] = fmt([
    { title: "Conversational Preposition Stranding (End of Question)", text: "What kind of music do you like listening to?" },
    { title: "Formal Preposition Placement (Before Pronoun)", text: "To whom should this official letter be addressed?" }
]);

// Lessons 87 - 120
customMap[87] = fmt([
    { title: "Borrow (take temporarily)", text: "Can I borrow your umbrella for an hour?" },
    { title: "Lend (give temporarily)", text: "I will lend you my laptop for the presentation." },
    { title: "Keep (hold permanently/long-term)", text: "You can keep the souvenir; it is a gift for you." }
]);

customMap[88] = fmt([
    { title: "Rob (victim or place targeted)", text: "The thieves robbed the downtown bank yesterday." },
    { title: "Steal (specific item taken)", text: "Someone stole my leather wallet from my jacket." }
]);

customMap[89] = fmt([
    { title: "Study (reading / practicing / process)", text: "I study English for an hour every night." },
    { title: "Learn (acquiring knowledge or skill)", text: "She learned how to swim when she was six." },
    { title: "Teach (imparting knowledge to others)", text: "He teaches mathematics at the local high school." }
]);

customMap[90] = fmt([
    { title: "Rise (intransitive: go up by itself)", text: "The sun rises in the east every morning." },
    { title: "Raise (transitive: lift or increase something)", text: "Please raise your hand if you have a question." },
    { title: "Arise (happen or come into notice)", text: "If any unexpected problems arise, let me know immediately." }
]);

customMap[91] = fmt([
    { title: "Lie (recline / rest - intransitive)", text: "I like to lie on the couch and listen to music." },
    { title: "Lay (place something down - transitive)", text: "Please lay the books gently on the wooden desk." },
    { title: "Past Tense of Lie (lay)", text: "Yesterday, he lay on the grass for an hour." },
    { title: "Past Tense of Lay (laid)", text: "She laid her phone on the counter." }
]);

customMap[92] = fmt([
    { title: "Say (without personal object)", text: "She said that she would arrive by noon." },
    { title: "Tell (with personal object)", text: "He told me the good news right away." },
    { title: "Speak / Talk (languages or general conversation)", text: "We spoke about the upcoming travel plans." }
]);

customMap[93] = fmt([
    { title: "Hear (passive acoustic perception)", text: "Did you hear that loud noise outside?" },
    { title: "Listen (active focused attention with 'to')", text: "Listen carefully to the instructions before starting." }
]);

customMap[94] = fmt([
    { title: "See (automatic visual perception)", text: "I saw an old friend at the supermarket." },
    { title: "Look (directing eyes with 'at')", text: "Look at this beautiful sunset over the mountains!" },
    { title: "Watch (paying attention to movement over time)", text: "We watched a fascinating documentary last night." }
]);

customMap[95] = fmt([
    { title: "Do (actions, obligations, jobs, routines)", text: "I need to do my homework and do the laundry." },
    { title: "Make (creating, producing, causing results)", text: "She made a delicious dinner and made a great decision." }
]);

customMap[96] = fmt([
    { title: "Bring (movement toward speaker)", text: "Please bring me a glass of water." },
    { title: "Take (movement away from speaker)", text: "Take these documents to the accounting department." },
    { title: "Fetch (go, get, and return)", text: "The dog ran to fetch the wooden stick." }
]);

customMap[97] = fmt([
    { title: "Come (movement toward speaker/listener)", text: "Come over to my house for dinner." },
    { title: "Go (movement away from current place)", text: "We are going to the beach this afternoon." }
]);

customMap[98] = fmt([
    { title: "Remember (recalling memory yourself)", text: "I remember visiting this town as a child." },
    { title: "Remind (causing someone else to remember)", text: "Please remind me to call the doctor tomorrow." }
]);

customMap[99] = fmt([
    { title: "Advise (verb /ədˈvaɪz/)", text: "I strongly advise you to consult a lawyer." },
    { title: "Advice (uncountable noun /ədˈvaɪs/)", text: "She gave me some excellent career advice." }
]);

customMap[100] = fmt([
    { title: "Affect (verb: to influence)", text: "The weather will affect our outdoor picnic plans." },
    { title: "Effect (noun: result or consequence)", text: "The new policy had a positive effect on productivity." }
]);

customMap[101] = fmt([
    { title: "Accept (verb: receive or agree to)", text: "I gladly accept your invitation to the anniversary dinner." },
    { title: "Except (preposition: excluding)", text: "Everyone arrived on time for the meeting except for Mark." }
]);

customMap[102] = fmt([
    { title: "Loose (adjective: not tight)", text: "This sweater is very loose on me after losing weight." },
    { title: "Lose (verb: misplace or fail to win)", text: "Be careful not to lose your passport at the airport." }
]);

customMap[103] = fmt([
    { title: "Everyday (adjective: ordinary / routine)", text: "These are my everyday shoes for walking in the neighborhood." },
    { title: "Every day (adverbial phrase: each day)", text: "I practice speaking English every day for thirty minutes." }
]);

customMap[104] = fmt([
    { title: "Then (time, sequence, or consequence)", text: "We went to dinner, and then we watched a movie." },
    { title: "Than (comparison)", text: "She is taller than her elder brother." }
]);

customMap[105] = fmt([
    { title: "Their (possessive pronoun)", text: "The students forgot their textbooks in the classroom." },
    { title: "There (location / existence)", text: "There is a quiet park right around the corner." },
    { title: "They're (contraction for 'they are')", text: "They're planning to visit us during the winter holidays." }
]);

customMap[106] = fmt([
    { title: "Its (possessive form)", text: "The puppy wagged its tail happily." },
    { title: "It's (contraction for 'it is' / 'it has')", text: "It's going to be a sunny day today." }
]);

customMap[107] = fmt([
    { title: "Your (possessive pronoun)", text: "Is this your laptop on the table?" },
    { title: "You're (contraction for 'you are')", text: "You're going to do fantastic on the exam!" }
]);

customMap[108] = fmt([
    { title: "Whose (possessive form)", text: "Whose coat is left hanging on the chair?" },
    { title: "Who's (contraction for 'who is' / 'who has')", text: "Who's coming to the dinner party tonight?" }
]);

customMap[109] = fmt([
    { title: "Stationary (standing still / fixed)", text: "The exercise bike remains stationary in the room." },
    { title: "Stationery (writing paper and envelopes)", text: "She bought elegant personalized stationery for writing letters." }
]);

customMap[110] = fmt([
    { title: "Principal (chief / school head / main)", text: "The school principal delivered an inspiring speech." },
    { title: "Principle (fundamental rule or moral value)", text: "He refuses to compromise on his core principles." }
]);

customMap[111] = fmt([
    { title: "Beside (by the side of / next to)", text: "She sat beside her best friend during the concert." },
    { title: "Besides (in addition to / moreover)", text: "Besides English, she speaks Spanish and French." }
]);

customMap[112] = fmt([
    { title: "Between (distinct individual items / usually two)", text: "Choose between the blue shirt and the white one." },
    { title: "Among (group or collective body of three or more)", text: "The treasure was divided among the four sailors." }
]);

customMap[113] = fmt([
    { title: "Sometime (unspecified point in time)", text: "Let's grab coffee together sometime next week." },
    { title: "Sometimes (occasionally / at times)", text: "Sometimes I prefer staying home rather than going out." },
    { title: "Some time (a period or duration of time)", text: "I need some time to think about your proposal." }
]);

customMap[114] = fmt([
    { title: "Altogether (completely / entirely)", text: "That is an altogether different situation." },
    { title: "All together (gathered in one place as a group)", text: "Let's sing the chorus all together now!" }
]);

customMap[115] = fmt([
    { title: "Compliment (praise or admiration)", text: "He paid her a lovely compliment on her artwork." },
    { title: "Complement (to complete or pair well with)", text: "The red wine complements the steak perfectly." }
]);

customMap[116] = fmt([
    { title: "Emigrate (leave home country)", text: "My ancestors emigrated from Italy in 1910." },
    { title: "Immigrate (move into new country permanently)", text: "They immigrated to Canada to start a new business." }
]);

customMap[117] = fmt([
    { title: "Historic (famous / important in history)", text: "Today is a historic moment for space exploration." },
    { title: "Historical (related to facts/study of history)", text: "She loves reading historical fiction novels." }
]);

customMap[118] = fmt([
    { title: "Economic (relating to economy / finance)", text: "The government introduced new economic reforms." },
    { title: "Economical (thrifty / inexpensive / avoiding waste)", text: "Driving a hybrid car is very economical on fuel." }
]);

customMap[119] = fmt([
    { title: "Sensible (reasonable / practical / wise)", text: "It is sensible to save money for emergency expenses." },
    { title: "Sensitive (easily hurt / empathetic / delicate)", text: "Be careful what you say; he is very sensitive to criticism." }
]);

customMap[120] = fmt([
    { title: "Desert (arid dry land / abandon)", text: "Camels can survive in the harsh desert environment." },
    { title: "Dessert (sweet dish after a meal)", text: "We ordered chocolate lava cake for dessert." }
]);

// Lessons 121 - 138
customMap[121] = fmt([
    { title: "Quiet (silent / low noise)", text: "Please be quiet while the test is in progress." },
    { title: "Quite (fairly / completely)", text: "It was quite cold outside yesterday evening." },
    { title: "Quit (stop / resign)", text: "He decided to quit smoking for his health." }
]);

customMap[122] = fmt([
    { title: "Capital (chief city / wealth / uppercase)", text: "Paris is the capital of France, and start your name with a capital letter." },
    { title: "Capitol (legislative building)", text: "The congress met inside the state capitol building." }
]);

customMap[123] = fmt([
    { title: "Continuous (unbroken without interruption)", text: "The rainfall was continuous for five full hours." },
    { title: "Continual (occurring repeatedly with breaks)", text: "He suffered from continual interruptions throughout the workday." }
]);

customMap[124] = fmt([
    { title: "Farther (measurable physical distance)", text: "He can run farther down the track than I can." },
    { title: "Further (figurative degree / additional info)", text: "Please contact our support office for further information." }
]);

customMap[125] = fmt([
    { title: "Fewer (for plural countable nouns)", text: "There were fewer attendees at the meeting today than yesterday." },
    { title: "Less (for singular uncountable nouns)", text: "Please add less sugar to my iced tea." }
]);

customMap[126] = fmt([
    { title: "Number (for countable nouns)", text: "A large number of students attended the seminar." },
    { title: "Amount (for uncountable nouns)", text: "They spent a significant amount of money on renovations." }
]);

customMap[127] = fmt([
    { title: "Uninterested (not interested / bored)", text: "He seemed completely uninterested in watching the game." },
    { title: "Disinterested (impartial / unbiased)", text: "A judge must remain a disinterested party in court." }
]);

customMap[128] = fmt([
    { title: "Person (singular individual)", text: "One person left their umbrella in the hallway." },
    { title: "People (plural human beings)", text: "Many people enjoy traveling during the summer." },
    { title: "Persons (formal legal usage)", text: "Authorized persons only are permitted past this door." },
    { title: "Peoples (distinct ethnic groups/nations)", text: "The indigenous peoples of North America have rich heritage." }
]);

customMap[129] = fmt([
    { title: "lay vs lie (place vs recline)", text: "Lay the blanket on the bed, and then lie down to rest." },
    { title: "raise vs rise (lift vs go up)", text: "Raise your hand when you see the temperature rise." },
    { title: "borrow vs lend (take vs give)", text: "Can I borrow a pen, or can you lend me one?" }
]);

customMap[130] = fmt([
    { title: "Subject Pronoun (I, he, she, we, they)", text: "She and I completed the project ahead of schedule." },
    { title: "Object Pronoun (me, him, her, us, them)", text: "The manager invited him and me to present the findings." }
]);

customMap[131] = fmt([
    { title: "Possessive Adjective (my, your, his, her, our, their + Noun)", text: "This is my jacket on the coat rack." },
    { title: "Possessive Pronoun (mine, yours, his, hers, ours, theirs)", text: "That blue umbrella over there is mine." }
]);

customMap[132] = fmt([
    { title: "Reflexive Pronoun (Subject = Object)", text: "He accidentally cut himself while slicing vegetables." },
    { title: "Emphatic Pronoun (Adding Emphasis)", text: "The president himself delivered the opening address." }
]);

customMap[133] = fmt([
    { title: "Who (subject for people)", text: "The artist who painted this mural lives in Chicago." },
    { title: "Whom (object for people - formal)", text: "To whom should I direct this inquiry?" },
    { title: "Whose (possessive for people/things)", text: "I met a writer whose book became a bestseller." },
    { title: "Which (non-essential information for things)", text: "My laptop, which I bought last year, is working great." },
    { title: "That (essential clause for people/things)", text: "The document that you requested is attached." }
]);

customMap[134] = fmt([
    { title: "Defining Relative Clause (Essential - No Commas)", text: "The doctor who treated my knee was very kind." },
    { title: "Non-Defining Relative Clause (Extra Details - With Commas)", text: "My brother, who lives in Seattle, is visiting us next week." }
]);

customMap[135] = fmt([
    { title: "Someone / Somebody (affirmative context)", text: "Someone left a notebook on the library desk." },
    { title: "Anyone / Anybody (negative/question context)", text: "Did anybody call while I was out of the office?" },
    { title: "Everyone / Everybody (singular verb for all)", text: "Everyone is excited about the company retreat." },
    { title: "Nothing / Nobody (negative meaning)", text: "Nothing is impossible if you work hard." }
]);

customMap[136] = fmt([
    { title: "This / These (Near in space or present time)", text: "This book in my hand is fascinating, and these shoes are comfortable." },
    { title: "That / Those (Distant in space or past/future time)", text: "That car parked over there is electric, and those days were wonderful." }
]);

customMap[137] = fmt([
    { title: "Each other (Mutual action between two subjects)", text: "The two brothers always support each other." },
    { title: "One another (Mutual action among three or more)", text: "Team members should communicate openly with one another." }
]);

customMap[138] = fmt([
    { title: "Dummy Subject 'It' (weather / time / distance)", text: "It is six o'clock, and it is starting to snow outside." },
    { title: "Dummy Subject 'There' (existence of items)", text: "There are three reasons why we chose this plan." }
]);

customMap[139] = fmt([
    { title: "Correct: John and I (Subject)", text: "John and I went to the conference together." },
    { title: "Correct: John and me (Object)", text: "The director greeted John and me warmly." },
    { title: "Correct: Reflexive self (himself/myself)", text: "He completed the entire repair himself." }
]);

customMap[140] = fmt([
    { title: "Gerund after certain verbs (enjoy + -ing)", text: "I enjoy swimming in the ocean on warm summer days." },
    { title: "Infinitive after certain verbs (want + to verb)", text: "I want to learn how to play the guitar." }
]);

customMap[141] = fmt([
    { title: "stop to do (pause in order to do something)", text: "He stopped to buy a newspaper on his way home." },
    { title: "stop doing (quit the habit or activity)", text: "He stopped smoking three years ago." },
    { title: "remember to do (future duty / task)", text: "Remember to lock the front door before leaving." },
    { title: "remember doing (past memory)", text: "I distinctly remember locking the front door." }
]);

customMap[142] = fmt([
    { title: "-ed Adjectives (describes how a person feels)", text: "I feel bored during long lectures." },
    { title: "-ing Adjectives (describes the cause/situation)", text: "The long lecture was very boring." }
]);

customMap[143] = fmt([
    { title: "used to + base verb (past habit or state)", text: "I used to live in London when I was a student." },
    { title: "didn't use to (negative past habit)", text: "She didn't use to drink coffee, but now she loves it." }
]);

customMap[144] = fmt([
    { title: "be used to + noun / gerund (current familiarity)", text: "I am used to waking up early at 6 AM every day." },
    { title: "not be used to (unfamiliar / new feeling)", text: "He is not used to driving on the left side of the road." }
]);

customMap[145] = fmt([
    { title: "get used to + noun / gerund (process of adapting)", text: "I am getting used to the cold weather in Chicago." },
    { title: "will get used to (future adaptation)", text: "Don't worry, you will soon get used to your new schedule." }
]);

customMap[146] = fmt([
    { title: "used to (past discontinued habit)", text: "I used to eat fast food every day." },
    { title: "be used to (current accustomed state)", text: "I am used to eating a healthy breakfast." },
    { title: "get used to (gradual process of accustomedness)", text: "I am getting used to cooking my own meals." }
]);

// Lessons 147 - 167
customMap[147] = fmt([
    { title: "a (before consonant sound)", text: "He bought a university textbook and a European car." },
    { title: "an (before vowel sound)", text: "She waited for an hour to speak with an honest official." }
]);

customMap[148] = fmt([
    { title: "the (specific known item)", text: "Please pass me the key on the kitchen table." },
    { title: "the (unique world objects / superlatives)", text: "The sun sets in the west; it is the tallest building." }
]);

customMap[149] = fmt([
    { title: "Zero Article (general plural / uncountable nouns)", text: "Milk is good for growing bones." },
    { title: "Zero Article (languages / academic subjects)", text: "She excels at mathematics and speaks fluent Italian." }
]);

customMap[150] = fmt([
    { title: "some (affirmative statements & polite offers)", text: "I bought some fresh apples. Would you like some tea?" },
    { title: "any (negative statements & general questions)", text: "I don't have any questions. Do you have any milk left?" }
]);

customMap[151] = fmt([
    { title: "many (countable nouns)", text: "There aren't many open seats left in the auditorium." },
    { title: "much (uncountable nouns in questions/negatives)", text: "How much free time do you have this afternoon?" },
    { title: "a lot of (both countable & uncountable)", text: "She has a lot of friends and a lot of confidence." }
]);

customMap[152] = fmt([
    { title: "a few (small positive amount - countable)", text: "I have a few friends coming over tonight." },
    { title: "few (almost none - negative connotation)", text: "Regrettably, few people attended the morning lecture." },
    { title: "a little (small positive amount - uncountable)", text: "I need a little help with this math problem." },
    { title: "little (almost none - negative connotation)", text: "We have little hope of reaching the station on time." }
]);

customMap[153] = fmt([
    { title: "Each (individual focus / two or more)", text: "Each student received an award individually." },
    { title: "Every (group collective focus / three or more)", text: "Every employee in the company attended the meeting." }
]);

customMap[154] = fmt([
    { title: "Both (two items together - plural verb)", text: "Both candidates are highly qualified for the position." },
    { title: "Either (one or the other - singular verb)", text: "You can choose either candidate for the interview." },
    { title: "Neither (not one nor the other - singular verb)", text: "Neither answer is completely correct." }
]);

customMap[155] = fmt([
    { title: "All (plural or uncountable nouns)", text: "All students must submit their assignments today." },
    { title: "Whole (entire singular countable noun)", text: "I spent the whole day reading a captivating book." },
    { title: "No (zero quantity before noun)", text: "There is no water left in the jug." }
]);

customMap[156] = fmt([
    { title: "the (oceans, rivers, mountain ranges, plural countries)", text: "The Amazon River flows into the Atlantic Ocean near the USA." },
    { title: "Zero Article (single lakes, single mountains, most countries)", text: "Mount Everest is located in Nepal near Lake Baikal." }
]);

customMap[157] = fmt([
    { title: "Reduced Article Sound ('a' -> /ə/)", text: "I bought a /ə/ new book at the shop." },
    { title: "Linked Definite Article ('the' before vowel -> /ðiː/)", text: "Look at the /ðiː/ elephant standing near the trees." }
]);

customMap[158] = fmt([
    { title: "Proper Nouns & Names", text: "Dr. Sarah Jenkins lives in London, England." },
    { title: "Days, Months & Holidays", text: "We are celebrating Thanksgiving on Thursday in November." },
    { title: "Nationalities & Languages", text: "He is studying Japanese literature and culture." }
]);

customMap[159] = fmt([
    { title: "Items in a Series / List", text: "We bought fresh apples, oranges, bananas, and grapes." },
    { title: "Introductory Clause Separation", text: "Although it was raining, we enjoyed our morning walk." },
    { title: "Coordinating Conjunction Clause Connection", text: "She wanted to go to the beach, but he preferred the mountains." }
]);

customMap[160] = fmt([
    { title: "Contractions (it is -> it's)", text: "It's a wonderful day for an outdoor concert." },
    { title: "Singular Possessive (dog's bone)", text: "The dog's collar was bright red." },
    { title: "Plural Possessive (teachers' lounge)", text: "The teachers' lounge is located on the second floor." }
]);

customMap[161] = fmt([
    { title: "Direct Speech Quote Formatting", text: "\"I will meet you at the library,\" said Maria." },
    { title: "Commas & Periods Inside Quotations", text: "He said, \"The report is ready,\" and handed over the file." }
]);

customMap[162] = fmt([
    { title: "Semicolon (joining related independent clauses)", text: "The store was closed; we had to come back the next day." },
    { title: "Colon (introducing a detailed list)", text: "Please bring three items: a notebook, a pen, and an eraser." }
]);

customMap[163] = fmt([
    { title: "Hyphen in Compound Adjective (before noun)", text: "She is a well-known international author." },
    { title: "Em-Dash for Strong Parenthetical Emphasis", text: "The team—despite all odds—won the championship." }
]);

customMap[164] = fmt([
    { title: "Direct Question Mark", text: "What time does the conference start tomorrow?" },
    { title: "Exclamation Mark for Emotion / Urgency", text: "Watch out for that slippery ice!" }
]);

customMap[165] = fmt([
    { title: "Parentheses () for Supplementary Info", text: "The annual report (published in March) shows strong growth." },
    { title: "Square Brackets [] for Editorial Clarification", text: "He said, \"They [the engineers] fixed the issue immediately.\"" }
]);

customMap[166] = fmt([
    { title: "Avoiding Comma Splice (Incorrect)", text: "Incorrect: I ran fast, I missed the bus." },
    { title: "Correct Semicolon / Conjunction Fix", text: "Correct: I ran fast; however, I missed the bus." }
]);

customMap[167] = fmt([
    { title: "Comma Pause Cue", text: "When you speak, take a brief breath at every comma." },
    { title: "Period Pitch Drop Cue", text: "Lower your voice pitch slightly at the end of a complete sentence." }
]);

// Lessons 168 - 184
customMap[168] = fmt([
    { title: "Active Voice (Subject Performs Action)", text: "The chef cooked a delicious gourmet meal." },
    { title: "Passive Voice (Subject Receives Action)", text: "A delicious gourmet meal was cooked by the chef." }
]);

customMap[169] = fmt([
    { title: "Present Simple Passive (am/is/are + V3)", text: "Letters are delivered to our office daily at 10 AM." },
    { title: "Past Simple Passive (was/were + V3)", text: "The historic bridge was constructed in 1895." }
]);

customMap[170] = fmt([
    { title: "Modal Passive (Modal + Be + Past Participle)", text: "The official contract must be signed before Friday." },
    { title: "Can Be Done / Should Be Sent", text: "The feedback forms should be sent to HR immediately." }
]);

customMap[171] = fmt([
    { title: "Formal / Technical Report Usage", text: "The chemical sample was analyzed under controlled laboratory conditions." },
    { title: "Agent Unknown or Unimportant", text: "My stolen wallet was found lying near the subway entrance." }
]);

customMap[172] = fmt([
    { title: "Separable Phrasal Verb", text: "Please turn on the light / Please turn the light on." },
    { title: "Pronoun Rule (MUST go in between)", text: "Please turn it on (NOT 'turn on it')." },
    { title: "Inseparable Phrasal Verb", text: "I ran into my former teacher at the library." }
]);

customMap[173] = fmt([
    { title: "get along with (have a good relationship)", text: "I get along very well with my coworkers." },
    { title: "get over (recover from illness or sadness)", text: "It took her weeks to get over the flu." },
    { title: "get by (survive financially)", text: "We can easily get by on a modest budget." }
]);

customMap[174] = fmt([
    { title: "take off (depart or remove clothing)", text: "The plane will take off in twenty minutes." },
    { title: "take over (assume control)", text: "The new manager will take over the department next week." },
    { title: "take after (resemble a family member)", text: "She really takes after her mother in temperament." }
]);

customMap[175] = fmt([
    { title: "turn down (refuse offer / lower volume)", text: "He turned down the job offer because of the long commute." },
    { title: "turn up (appear unexpectedly / increase volume)", text: "He surprisingly turned up at the party late." },
    { title: "turn out (result in / prove to be)", text: "The event turned out to be a great success." }
]);

customMap[176] = fmt([
    { title: "look after (take care of)", text: "She stayed home to look after her sick puppy." },
    { title: "look forward to (+ gerund / noun)", text: "I am looking forward to visiting you next month." },
    { title: "look up (search for information)", text: "You should look up new vocabulary words in the dictionary." }
]);

customMap[177] = fmt([
    { title: "bring up (raise a topic / raise a child)", text: "She brought up an interesting point during the meeting." },
    { title: "give up (quit a habit or stop trying)", text: "Never give up on achieving your career goals." },
    { title: "give away (donate or reveal secret)", text: "Please don't give away the ending of the movie!" }
]);

customMap[178] = fmt([
    { title: "come across (find unexpectedly)", text: "I came across an old photo album while cleaning the attic." },
    { title: "come up with (invent / suggest an idea)", text: "Our team came up with a creative solution to the problem." },
    { title: "go through (experience or examine carefully)", text: "He went through a challenging phase last year." }
]);

customMap[179] = fmt([
    { title: "put off (postpone / delay)", text: "Never put off until tomorrow what you can do today." },
    { title: "put up with (tolerate / endure)", text: "I cannot put up with this loud noise any longer." },
    { title: "put on (wear clothing)", text: "Put on your heavy jacket before going outside." }
]);

customMap[180] = fmt([
    { title: "call off (cancel an event)", text: "They had to call off the soccer game due to thunderstorm." },
    { title: "break down (stop functioning / emotional outburst)", text: "My car broke down on the highway yesterday." },
    { title: "break out (start suddenly)", text: "A fire broke out in the old warehouse late at night." }
]);

customMap[181] = fmt([
    { title: "carry out (execute a plan / task)", text: "The team will carry out the marketing strategy next month." },
    { title: "follow up on (check status / progress)", text: "I will follow up on your request with the IT department." },
    { title: "set up (arrange meeting / equipment)", text: "She set up a video conference for Tuesday morning." },
    { title: "wind up (conclude / wrap up)", text: "Let's wind up the meeting by summarizing our key goals." }
]);

customMap[182] = fmt([
    { title: "run out of (deplete supply completely)", text: "We ran out of printer paper right before the presentation." },
    { title: "catch up with (reach same level / talk)", text: "I need to catch up with my old college roommate." },
    { title: "cut down on (reduce consumption)", text: "I am trying to cut down on drinking sugary soda." }
]);

customMap[183] = fmt([
    { title: "check in (register at hotel / airport)", text: "We checked in at the hotel front desk upon arrival." },
    { title: "drop off (deliver someone / something)", text: "Can you drop me off at the train station on your way?" },
    { title: "pick up (collect someone / something)", text: "I will pick up the dry cleaning after work today." },
    { title: "see off (say goodbye at departure)", text: "The whole family went to the airport to see him off." }
]);

customMap[184] = fmt([
    { title: "Natural Spoken Flow (hang out)", text: "Let's hang out at the park this Sunday afternoon." },
    { title: "Conversational Phrasal Verb (figure out)", text: "We managed to figure out the puzzle together." }
]);

// Lessons 185 - 234
customMap[185] = fmt([
    { title: "Prefix Expansion (un-, re-, mis-)", text: "unhappy (not happy), rewrite (write again), misunderstand (understand wrongly)." },
    { title: "Suffix Expansion (-ful, -less, -ize)", text: "careful (full of care), careless (without care), modernize (make modern)." }
]);

customMap[186] = fmt([
    { title: "Simple Sentence (One Independent Clause)", text: "The rain stopped after two hours." },
    { title: "Compound Sentence (FANBOYS Connection)", text: "The rain stopped, and the sun came out brightly." },
    { title: "Complex Sentence (Subordinating Conjunction)", text: "When the rain stopped, we went for a walk in the garden." }
]);

customMap[187] = fmt([
    { title: "SVOMPT Rule (Subject + Verb + Object + Manner + Place + Time)", text: "He played the piano beautifully in the auditorium last night." },
    { title: "Standard Word Order Example", text: "She read the book carefully at home yesterday evening." }
]);

customMap[188] = fmt([
    { title: "Direct Speech", text: "He said, \"I am working late tonight.\"" },
    { title: "Reported Speech (Backshifted Tense)", text: "He said that he was working late that night." }
]);

customMap[189] = fmt([
    { title: "Inversion with 'Never'", text: "Never have I seen such a breathtaking view." },
    { title: "Inversion with 'Rarely / Seldom'", text: "Rarely does he complain about his long working hours." },
    { title: "Inversion with 'Hardly... when'", text: "Hardly had I entered the room when the phone rang." }
]);

customMap[190] = fmt([
    { title: "Formal Email Etiquette", text: "Dear Mr. Smith, I am writing to inquire regarding..." },
    { title: "Informal Conversational Style", text: "Hi Mark, just checking in to see how you are doing!" }
]);

customMap[191] = fmt([
    { title: "Adverb of Manner (how action is done)", text: "She sang the ballad softly and gracefully." },
    { title: "Adverb of Degree (intensity)", text: "The climate was extremely hot during July." },
    { title: "Adverb of Frequency (how often)", text: "I usually go jogging three times a week." }
]);

customMap[192] = fmt([
    { title: "Standard Intensifiers (very, really, extremely)", text: "It was a very cold winter morning." },
    { title: "Intensifiers with Extreme Adjectives (absolutely + freezing)", text: "The weather outside was absolutely freezing!" },
    { title: "Moderate Intensifiers (quite, fairly)", text: "The exam was fairly easy for most prepared students." }
]);

customMap[193] = fmt([
    { title: "Gaining Thinking Time (Well / Actually)", text: "Well, actually, I haven't considered that option yet." },
    { title: "Softening Statement (You know / As a matter of fact)", text: "As a matter of fact, I met him at the conference yesterday." }
]);

customMap[194] = fmt([
    { title: "In my opinion...", text: "In my opinion, investing in green energy is vital." },
    { title: "From my perspective...", text: "From my perspective, this new policy will benefit everyone." },
    { title: "I would suggest...", text: "I would suggest taking a moment to review the data." }
]);

customMap[195] = fmt([
    { title: "Polite Agreement", text: "I completely agree with your point of view." },
    { title: "Graceful Disagreement", text: "I see your point, but I have a slightly different view." }
]);

customMap[196] = fmt([
    { title: "Asking to Rephrase", text: "Could you please rephrase that last sentence?" },
    { title: "Checking Understanding", text: "If I understand correctly, you mean we should postpone the deadline?" },
    { title: "Asking for Detail", text: "Could you elaborate a bit more on that specific point?" }
]);

customMap[197] = fmt([
    { title: "Content Words Stressed (nouns, verbs, adjectives)", text: "We **visited** the **famous** **museum** in **Paris**." },
    { title: "Structure Words Unstressed (articles, prepositions)", text: "We went *to the* store *for a* bottle *of* milk." }
]);

customMap[198] = fmt([
    { title: "Falling Intonation (statements & Wh- questions)", text: "Where do you live? / I live in New York." },
    { title: "Rising Intonation (Yes/No questions & checking)", text: "Are you coming to the party tonight?" }
]);

customMap[199] = fmt([
    { title: "Linking Consonant to Vowel ('an apple' -> /ə-næpəl/)", text: "She ate an apple for her morning snack." },
    { title: "Linking 'check it out' -> /tʃɛ-kɪ-taʊt/", text: "Hey, check it out on the notice board!" }
]);

customMap[200] = fmt([
    { title: "Elision (dropping sound: 'next door' -> 'nexdoor')", text: "My neighbor lives right next door." },
    { title: "Blending (going to -> gonna / want to -> wanna)", text: "I'm gonna study hard because I wanna pass." }
]);

customMap[201] = fmt([
    { title: "Formal Alternative: 'big' -> 'substantial'", text: "The company reported a substantial increase in quarterly revenue." },
    { title: "Formal Alternative: 'change' -> 'modify'", text: "We need to modify our strategy to reach international markets." },
    { title: "Formal Alternative: 'help' -> 'assist'", text: "Our representative will assist you with the application." },
    { title: "Formal Alternative: 'show' -> 'demonstrate'", text: "The experimental results clearly demonstrate high efficiency." }
]);

customMap[202] = fmt([
    { title: "hit the nail on the head (be exactly right)", text: "Your diagnosis of the problem hit the nail on the head." },
    { title: "piece of cake (very easy task)", text: "The driving test was a piece of cake for her." },
    { title: "break the ice (initiate friendly conversation)", text: "He told a joke to break the ice at the start of the seminar." }
]);

customMap[203] = fmt([
    { title: "OSASCOMP Adjective Order (Opinion, Size, Age, Shape, Color, Origin, Material, Purpose)", text: "She bought a beautiful small old round black Italian wooden writing table." },
    { title: "Simplified Example", text: "He drives a handsome new red German sports car." }
]);

customMap[204] = fmt([
    { title: "Comparative (short adjectives: -er)", text: "My new laptop is much faster than my old one." },
    { title: "Comparative (long adjectives: more)", text: "This landscape painting is more beautiful than that one." },
    { title: "Superlative (-est / most)", text: "She is the most talented singer in the national choir." },
    { title: "Irregular Forms (good/better/best, bad/worse/worst)", text: "His second draft was much better than the first." }
]);

customMap[205] = fmt([
    { title: "Singular Subject + Singular Verb", text: "The box of heavy books rests on the table." },
    { title: "Plural Subject + Plural Verb", text: "The boxes of heavy books rest on the table." }
]);

customMap[206] = fmt([
    { title: "Collective Noun as Unified Unit (Singular)", text: "The committee has reached a unanimous decision." },
    { title: "Collective Noun as Individual Members (Plural)", text: "The committee members are arguing among themselves." }
]);

customMap[207] = fmt([
    { title: "Standard Single Negative (Correct)", text: "I don't know anything about that issue." },
    { title: "Avoiding Double Negative Error", text: "Incorrect: 'I don't know nothing' -> Correct: 'I know nothing'." }
]);

customMap[208] = fmt([
    { title: "Singular Antecedent Alignment", text: "Every student must submit their homework on time." },
    { title: "Plural Antecedent Alignment", text: "All students must submit their homework on time." }
]);

customMap[209] = fmt([
    { title: "Dangling Modifier Error (Incorrect)", text: "Incorrect: Walking down the street, the trees looked pretty." },
    { title: "Corrected Attachment", text: "Correct: Walking down the street, I admired the pretty trees." }
]);

customMap[210] = fmt([
    { title: "Parallel Structure with Gerunds", text: "He enjoys swimming, running, and cycling." },
    { title: "Parallel Structure with Infinitives", text: "She wants to learn, to grow, and to succeed." }
]);

customMap[211] = fmt([
    { title: "Eliminating Redundancy: 'repeat again' -> 'repeat'", text: "Please repeat the instructions clearly." },
    { title: "Eliminating Redundancy: 'close proximity' -> 'close'", text: "The hotel is close to the airport." }
]);

customMap[212] = fmt([
    { title: "Common Error Fix: discuss (NO 'about')", text: "We will discuss the strategy during the meeting." },
    { title: "Common Error Fix: married to (NO 'with')", text: "She has been married to Mark for five years." },
    { title: "Common Error Fix: listen to (NEEDS 'to')", text: "I love listening to podcasts while driving." }
]);

customMap[213] = fmt([
    { title: "Consistent Past Narrative", text: "Yesterday, I woke up early, drank coffee, and walked to work." },
    { title: "Avoiding Abrupt Tense Shifts", text: "Maintain past tense throughout your story unless describing present facts." }
]);

customMap[214] = fmt([
    { title: "Action Verbs for Accomplishments", text: "I managed a team of ten engineers and delivered projects on time." },
    { title: "Avoid Overly Tentative Phrasing", text: "Use clear, confident verbs like 'spearheaded', 'orchestrated', and 'achieved'." }
]);

customMap[215] = fmt([
    { title: "Spoken English Convention (Contractions Acceptable)", text: "I'm gonna give it a try when I'm ready." },
    { title: "Written English Convention (Full Forms Required)", text: "I am going to give it an attempt when I am ready." }
]);

customMap[216] = fmt([
    { title: "Modal Verbs Checkup", text: "You should double-check all calculations before submission." },
    { title: "Prepositions & Articles Checkup", text: "She lives in an old house near the lake." }
]);

customMap[217] = fmt([
    { title: "Spoken Grammar Accuracy", text: "Neither the manager nor the staff members were informed." },
    { title: "Fluency & Intonation Mastery", text: "Speak naturally using connected speech and clear sentence rhythm." }
]);

customMap[218] = fmt([
    { title: "Happiness: on cloud nine", text: "She was on cloud nine after receiving her promotion." },
    { title: "Anger: see red", text: "He saw red when he noticed the damaged car." },
    { title: "Stress: under the weather", text: "I am feeling a bit under the weather today." }
]);

customMap[219] = fmt([
    { title: "hit the ground running (start with energy)", text: "The new marketing director hit the ground running on day one." },
    { title: "learn the ropes (master new skills)", text: "It takes a few weeks to learn the ropes in a new company." },
    { title: "burn the midnight oil (work late)", text: "We had to burn the midnight oil to finish the proposal." }
]);

customMap[220] = fmt([
    { title: "at a crossroads (facing a big decision)", text: "After graduating, she felt she was at a crossroads in life." },
    { title: "break the bank (cost too much)", text: "Eating healthy meals at home won't break the bank." },
    { title: "bite the bullet (face tough situation bravely)", text: "I decided to bite the bullet and go to the dentist." }
]);

customMap[221] = fmt([
    { title: "Meticulous (paying great attention to detail)", text: "She is a meticulous researcher who checks every fact twice." },
    { title: "Charismatic (inspiring charm and devotion)", text: "The charismatic leader captivated the entire audience." },
    { title: "Reliable (trustworthy and dependable)", text: "He is a reliable friend who is always there when needed." }
]);

customMap[222] = fmt([
    { title: "Tranquil (peaceful and calm)", text: "We spent a tranquil afternoon sitting beside the mountain lake." },
    { title: "Bustling (full of energetic activity)", text: "The bustling street market was crowded with shoppers." },
    { title: "Picturesque (charming like a picture)", text: "They visited a picturesque village nestled in the green hills." }
]);

customMap[223] = fmt([
    { title: "Upgrade: 'very tired' -> 'exhausted'", text: "After running the marathon, he felt completely exhausted." },
    { title: "Upgrade: 'very clean' -> 'spotless'", text: "The kitchen counter was so clean it was spotless." },
    { title: "Upgrade: 'very cold' -> 'freezing'", text: "Put on your thick coat; it is freezing outside!" },
    { title: "Upgrade: 'very smart' -> 'brilliant'", text: "She came up with a brilliant idea during the brainstorming session." }
]);

customMap[224] = fmt([
    { title: "Collocations with 'Take' (take a break / risk)", text: "Let's take a short ten-minute break before continuing." },
    { title: "Collocations with 'Have' (have a chat / impact)", text: "We had an engaging conversation regarding the project." },
    { title: "Collocations with 'Make' (make progress / effort)", text: "You have made significant progress in your English fluency." }
]);

customMap[225] = fmt([
    { title: "Collocations with 'Do' (do business / a favor)", text: "Could you do me a quick favor and hold this door?" },
    { title: "Collocations with 'Pay' (pay attention / compliment)", text: "Please pay close attention to the safety instructions." },
    { title: "Collocations with 'Keep' (keep a promise / in mind)", text: "Keep in mind that the office is closed on holidays." }
]);

customMap[226] = fmt([
    { title: "Expressing Sympathy", text: "I am so deeply sorry to hear about your loss." },
    { title: "Expressing Congratulations", text: "Warmest congratulations on winning first place!" },
    { title: "Expressing Praise", text: "Kudos to you and your team for a job exceptionally well done!" }
]);

customMap[227] = fmt([
    { title: "How about we try...?", text: "How about we try holding the meeting online instead?" },
    { title: "I strongly recommend that...", text: "I strongly recommend that we update our software immediately." },
    { title: "Why don't we consider...?", text: "Why don't we consider hiring a consultant for guidance?" }
]);

customMap[228] = fmt([
    { title: "To sum up...", text: "To sum up, our main priority is improving user experience." },
    { title: "In short...", text: "In short, we exceeded our sales targets for this quarter." },
    { title: "To put it another way...", text: "To put it another way, quality matters far more than quantity." }
]);

customMap[229] = fmt([
    { title: "It all started when...", text: "It all started when I received an unexpected email last week." },
    { title: "Out of nowhere...", text: "Out of nowhere, a heavy snowstorm began to fall." },
    { title: "To make a long story short...", text: "To make a long story short, we managed to catch the last train." }
]);

customMap[230] = fmt([
    { title: "Uncountable Noun Rule (No 'a/an' & No plural '-s')", text: "She gave me valuable advice regarding my career path." },
    { title: "Counting Phrase (a piece of / an item of)", text: "He carried three pieces of heavy luggage to the check-in desk." }
]);

customMap[231] = fmt([
    { title: "Irregular Vowel Change Plurals (man->men, tooth->teeth)", text: "Both men and women participated in the community clean-up." },
    { title: "Identical Singular & Plural (sheep, deer, fish)", text: "A flock of sheep was grazing peacefully on the hill." },
    { title: "Always-Plural Nouns (glasses, scissors, pants)", text: "Where did I place my reading glasses and sharp scissors?" }
]);

customMap[232] = fmt([
    { title: "Possessive 's for People & Animals", text: "John's briefcase was left in the conference room." },
    { title: "Possessive 'of' for Inanimate Objects", text: "The roof of the historic house was damaged by storm." },
    { title: "Compound Noun Plurals on Head Noun", text: "Several passers-by helped push the stalled vehicle." }
]);

customMap[233] = fmt([
    { title: "Make (force / obligation + base verb)", text: "The teacher made all students rewrite their essays." },
    { title: "Have (assign responsibility + base verb)", text: "I will have my assistant book your hotel room." },
    { title: "Get (persuade / convince + to-infinitive)", text: "I managed to get the technician to repair my laptop." },
    { title: "Let (allow / give permission + base verb)", text: "My parents let me borrow their car for the road trip." },
    { title: "Help (assist + base verb or to-infinitive)", text: "She helped me carry the heavy boxes up the stairs." }
]);

customMap[234] = fmt([
    { title: "Structure: suggest + -ing", text: "I suggest taking a short fifteen-minute walk to clear your mind." },
    { title: "Structure: suggest that + subject + base verb", text: "I suggest that he review the financial document before submitting it." },
    { title: "Structure: recommend that + subject + base verb", text: "The doctor recommended that she rest for three full days." }
]);

let missingCount = 0;
// Apply to all lessons
lessons.forEach((l, idx) => {
    const num = l.orderIndex || (idx + 1);
    if (customMap[num]) {
        l.exampleText = customMap[num];
    } else {
        missingCount++;
        console.warn(`Missing custom map for lesson ${num}`);
    }
});

console.log("Total missing lessons:", missingCount);

// Write result
fs.writeFileSync(path, JSON.stringify(lessons, null, 2), 'utf8');
console.log("Successfully mapped ALL 234 lessons with custom context examples!");
