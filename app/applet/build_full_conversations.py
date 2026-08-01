import json
import re

prompt_text = """
# Topic 1: Useful Adjectives

**Basic:**
Sophia: Please describe it. Is the mountain big or small?
Arun: It's big. That pebble is small.
Sophia: Is the bag heavy or light?
Arun: It's heavy. This one is light.
Sophia: Is the math difficult or easy?
Arun: 2+2=4 is easy. This is difficult.
Sophia: Is the car new or old?
Arun: This is new. That is old.
Sophia: Is the ring expensive or cheap?
Arun: It's expensive. This one is cheap.
Sophia: Is the frame wide or narrow?
Arun: This is wide. That is narrow.

**Intermediate:**
Priya: What's your coworker like? Is he boring or funny?
David: He's actually quite funny, but he gets a little anxious before big meetings.
Priya: Is he extroverted or introverted?
David: More extroverted. He's never bad-tempered, and he's always kind and nice.
Priya: Is he intelligent?
David: Very intelligent, though he can be indecisive when there are too many choices.
Priya: What about the taste of the food he brought?
David: It was a bit bitter and salty, but also strangely sweet. I love things that are spicy or sour, though — he should try that.

**Advanced:**
Sarah: I don't think anyone would call him boring — he's introverted, sure, but he's genuinely quite funny once you get past his anxious first impression.
Michael: Right, and unlike our old bad-tempered supervisor, he's remarkably kind, intelligent, and even-tempered under pressure, though admittedly a bit indecisive when the stakes are high.
Sarah: Speaking of contrasts, that dish had this fascinating umami depth beneath the salty crust — bitter on the first bite, then unexpectedly sweet, with just enough spicy heat and sour brightness to balance it all.
Michael: It reminds me of that old building downtown — dark, narrow, and a little ugly on the outside, yet strangely beautiful once you're inside. Meanwhile the new high-rise next to it is bright, wide, and expensive to even walk past. I got there early today, for once, instead of running late like usual.

---

# Topic 2: Around Town

**Basic:**
Priya: Where's the park?
Arun: It's near here.
Priya: Where's the church?
Arun: Next to the park.
Priya: I want to go to a restaurant.
Arun: There's one near city hall.
Priya: Where's the hospital?
Arun: Near the school.
Priya: Where's the police station, post office, museum, library, movie theater, and hotel?
Arun: They're all close together, downtown.

**Intermediate:**
David: Excuse me, I want to go to the museum. Do you know where it is?
Emma: Sure, it's right next to the library, downtown.
David: And the movie theater?
Emma: That's a few blocks past the hospital, close to the restaurant district.
David: What about city hall and the police station?
Emma: City hall is near the school, and the police station is just past the post office.
David: One more — where's a good hotel?
Emma: There's one near the church, not far from the park.

**Advanced:**
Sophia: I'm trying to plan my whole afternoon downtown — I need to stop by city hall, then swing past the post office, grab lunch near a decent restaurant, and still make it to the museum before it closes.
Michael: If you start at the park, you can walk past the church, then the hospital, and you'll hit the police station and school on the same street before looping back toward the library and movie theater.
Sophia: That's convenient — is the hotel within walking distance too, in case I want to check in early?
Michael: It is, actually, right between the library and the museum, so your whole itinerary fits into a fairly compact loop around downtown.

---

# Topic 3: Asking Directions, Buses, Trains & Taxis

**Basic:**
David: How do I get to the station?
Emma: Go straight, then turn left.
David: Is it far?
Emma: No, it's near here.
David: Does this bus go to downtown?
Driver: Yes.
David: What's the next stop?
Driver: City Hall.
David: How much is the fare?
Driver: Two dollars.
David: Turn right here, please.
Taxi Driver: Okay.
David: Here is fine.
Taxi Driver: Okay.

**Intermediate:**
Michael: Excuse me, do you know where the train station is?
Sophia: Yes, go straight for two blocks, then turn left at the corner.
Michael: Is there a bus stop near here too?
Sophia: Yes, next to the bank, opposite the pharmacy.
Michael: How do I get to the subway from there?
Sophia: Go past the bakery, and it's between the bank and the drugstore.
Michael: Great. How much is it to the airport by taxi?
Taxi Driver: About thirty dollars. Turn right here, please, then go straight, please.
Michael: Can you take me to this place instead? Actually, here is fine, thank you.

**Advanced:**
Arun: Is there a train station near here, or would it be faster to catch the subway from the corner?
Priya: Honestly, it depends — if you go straight and go past the old bus stop, you'll hit the train station in about ten minutes, but the subway entrance next to it is closer if you don't mind waiting.
Arun: What's the next stop after that on the subway line, and how much is the fare compared to the bus?
Priya: The next stop is downtown, and the fare's about the same either way. If you're in a hurry, though, I'd just take a taxi — tell the driver "turn right here, please" at the light, then "go straight, please" until you reach the hotel.
Arun: And if I just want to be dropped off early? Can you take me to this place, or should I just say "here is fine" once we're close?
Priya: Either works — most drivers are happy to stop wherever you say, whether that's "to this hotel, please" or somewhere along the way.

---

# Topic 4: At the Airport

**Basic:**
Sophia: Where's the baggage claim?
Staff: It's over there.
Sophia: Where's the waiting area?
Staff: Near the metal detector.
Sophia: Can you help me find my suitcase?
Staff: Yes, follow me.
Sophia: Where's the gate?
Staff: Gate 12, past the kiosk.
Sophia: Where's self check-in?
Staff: Near the check-in counter.
Sophia: Where's the departures board?
Staff: By the escalator.
Sophia: Where's the arrivals board?
Staff: On your right.

**Intermediate:**
David: Is this an international flight or a domestic flight?
Emma: International. We need to go through customs at the departure gate.
David: Do you have your passport and visa ready?
Emma: Yes, and my luggage is already checked.
Officer: Your passport, please.
Emma: Here's my passport.
Officer: What's the purpose of this trip?
Emma: Sightseeing.
Officer: How long will you be staying?
Emma: About two weeks.
Officer: What's inside your bag?
Emma: Only my personal belongings — these are presents from my friend.

**Advanced:**
Michael: Since it's an international flight, we'll need to clear customs before boarding, so make sure your passport and visa are both accessible, not buried in your suitcase.
Sophia: Already sorted — I checked my luggage at the counter, and I'm just deciding between the self check-in kiosk or the regular check-in counter, since the departures board says our gate desk hasn't opened yet.
Officer: Your passport, please. What's the purpose of this trip — is it business or sightseeing?
Michael: Sightseeing, mostly. How long will you be staying, you might ask — about two weeks, and everything inside my bag is just personal belongings, along with a few presents from my friend that I'm bringing back.
Sophia: While we wait, I might grab a keychain, a mug, or a T-shirt at the kiosk near the waiting area — maybe a magnet or postcard too, and I'll skip the alcohol this time since it's a hassle through the metal detector line.

---

# Topic 5: Business English

**Basic:**
Priya: What is in your office?
Arun: There is a refrigerator, a desk, and a chair.
Priya: Is there a whiteboard?
Arun: Yes, and a telephone and a stapler.
Priya: Is there a sofa?
Arun: Yes, and a keyboard and a monitor.
Priya: Is there a copier?
Arun: Yes, and a coffee maker and a microwave.

**Intermediate:**
Manager: Good morning, everyone. We're here today to discuss the new project. Let's begin, shall we?
David: I'd like to introduce our new client, Mr. Chen, the general manager.
Employee: What are your views on this proposal?
Manager: The way I see it, we should start with the budget.
Employee 2: I don't really agree — I think we should start with the timeline.
Manager: I didn't catch that. Could you repeat that, please?
Employee 2: Precisely, the timeline should come first.
Manager: I get your point. Thank you all for coming.

**Advanced:**
Manager: So, let's start with the numbers — I know some of you don't really agree with the projected budget, so let's hear it. What are your views on this?
Sarah: The way I see it, before we commit to any figures, we should bring in the section manager and the assistant general manager, since they'll be introducing this to the client directly — this is the section manager, Mr. Kim, by the way, and this is the assistant manager, Ms. Rao.
Manager: Fair point. I'd like to introduce everyone properly, then: this is our President, our executive managing director, our managing director, and our chief, all joining remotely for this portion.
Sarah: I didn't quite catch what the chief said earlier — could you repeat that, please? It sounded important.
Manager: Precisely what I was thinking. Well, thank you all for coming — remember, embrace the pain to inherit the gain, and if you want something done right, do it yourself, but also, give assistance, not advice, in a crisis. No rest for the weary this quarter, unfortunately — it really is another day, another dollar, but let's make hay while the sun shines.

---

# Topic 6: Shopping for Clothes

**Basic:**
Clerk: How may I help you?
Sophia: I'm looking for a sweater.
Clerk: What about a shirt or a T-shirt?
Sophia: Yes, and pants and shorts.
Clerk: Do you need underwear or a jacket?
Sophia: A jacket, please. Also a suit, a skirt, a dress, a blouse, and a hat.

**Intermediate:**
David: Can I try it on?
Clerk: Of course. The fitting room is right there.
David: Do you have this in a bigger size, or a different color?
Clerk: Let me check. Do you take credit cards, you're wondering? Yes, we do.
David: Can I exchange it if it doesn't fit?
Clerk: Yes, absolutely. Where is the fitting room, again — over there, past the mirrors.

**Advanced:**
Priya: I love to go shopping, but I hardly ever sell my old clothes — I mostly just look for pieces that match what I already have, then return the ones that don't work out once I get home.
Arun: Do you have a color you gravitate toward? I usually go for white, yellow, or green, though I'll wear gray, navy, blue, or black if the occasion calls for it.
Priya: This red shirt looked promising, but it's far too tight and the sleeves are too short — I need something looser and longer, not too big and not too small, which is surprisingly hard to find.
Arun: Have you asked if they have it in a bigger size, or checked whether they'll let you exchange it for a different color once you've had a chance to try it on properly in the fitting room?

---

# Topic 7: Making Complaints

**Basic:**
Noriko: What's the matter?
Mark: The office is hot.
Noriko: Is it ever cold?
Mark: Yes, sometimes cold.
Noriko: Is it noisy?
Mark: Yes, and dark and dirty too.

**Intermediate:**
Sarah: My coworker always causes a delay, and honestly, it's a bit rude.
Michael: Does he ever complain himself, or just make mistakes?
Sarah: He makes mistakes, and when I mention it, he says I'm being inconvenient, which really annoys me.
Michael: Did he insult you directly?
Sarah: No, but he did yell, and it felt like he was trying to reject my feedback and irritate me on purpose — it caused a real problem for the team.

**Advanced:**
Emma: Can I have a refund? It's too small, and honestly, the change was wrong when I paid, and I ended up with the wrong color anyway.
Clerk: I'm terribly sorry about that — let me look into both issues right away.
Emma: On top of that, it has a stain here, there's a hole here, and if I'm being completely honest, it even has a crack here. Can I exchange it for a new one instead of a refund?
Clerk: Absolutely, and I apologize again — issues like a stain, a hole, or a crack should never make it past our quality check, so let's get this sorted immediately.

---

# Topic 8: Dining Like a Champ

**Basic:**
David: I have a reservation at nine.
Host: Do you have a table for two?
David: Yes. May I have a menu?
Host: Here you go.
David: May I order? I'll have this, please.
Host: Sure.
David: Excuse me, my order hasn't come yet.
Host: Sorry, checking now.
David: Check, please.

**Intermediate:**
Waiter: Point and speak — just say "~ please." What are today's specials?
Sophia: We have chicken, beef, pork, salad, and seafood today.
Waiter: Would you like an appetizer, main dish, or dessert first?
Sophia: Main dish, please, and can I have a drink menu?
Waiter: Of course. Anything else — more napkins, perhaps?
Sophia: Yes, please. And counting — one, two, three, four, five of us tonight.

**Advanced:**
Priya: With meat, please, though without bell pepper — and could you check if this dish contains any peanuts, since I can't eat or drink alcohol either, so please remove the wine from the sauce if it's used.
Waiter: Understood — I'll also make sure there's no onion, cheese, or tomato, given your preferences, and I'll bring vegetables, butter, sugar, and olive oil on the side instead.
Priya: Perfect, and could you bring a spoon, a fork, a knife, and a napkin, along with some salt and black pepper? I'll also need water, bread, and coffee once the main dish arrives.
Waiter: Certainly. And for what it's worth, it's delicious, it looks tasty, and it's very good tonight — though I'll admit yesterday's batch was slightly overcooked and lacked salt; today's smells so nice and isn't the least bit raw or too spicy.

---

# Topic 9: Food Preferences, Allergies & Restaurant Vocabulary

**Basic:**
Sophia: I am allergic to shellfish.
David: I am a vegetarian.
Sophia: I am allergic to peanuts too.
David: I can't eat pork. My wife is a vegan.

**Intermediate:**
Emma: It's delicious! It looks tasty, and it's very good.
Michael: Really? Mine is overcooked, and it lacks salt.
Emma: That's a shame — mine smells so nice.
Michael: This is not fresh, and it's too spicy for me. Can you bring me a fork and a napkin, please? I need a spoon too.

**Advanced:**
Arun: Does this dish contain any shellfish, eggs, milk, wheat, or soy? I'm allergic to meat as well, oddly enough, and I can't eat or drink alcohol in any form.
Waiter: Let me double-check with the kitchen — we can absolutely remove ~ from this dish if needed, and prepare it without butter, cheese, or tomato as well.
Arun: I'd appreciate that. My friend here is a vegan, so please leave out fish entirely, and I need a knife, black pepper, and dessert menu once we're ready.
Waiter: Of course — and just to confirm, it's overcooked or raw complaints aside, tonight's dish smells so nice and isn't remotely too spicy, so I think you'll both be pleased.

---

# Topic 10: Emergency Words and Phrases

**Basic:**
Operator: This is 911. Please state the nature of your emergency.
Caller: I need a doctor! There is a fire!
Doctor: What's wrong?
Patient: My head hurts.
Patient: My stomach, tooth, knee, chest, back, ankle, and ear hurt too.
Patient: I'm suffering from asthma.

**Intermediate:**
Doctor: What's wrong? Does your stomach or your tooth hurt?
Patient: My knee hurts, and my chest feels tight.
Doctor: Any conditions — diabetes, an allergy, or heart disease?
Patient: I'm suffering from asthma, and I take medicine and antibiotics regularly.
Doctor: Have you had your vaccine, and do you have a prescription with you?
Patient: Yes, though the doctor said it might be a virus or bacteria this time.

**Advanced:**
Officer: In case of an emergency, when in the United States, dial 911 — this is 911, please state the nature of your emergency.
Tourist: There was an accident! I was robbed, and I want to report a crime — I'm being harassed and honestly, I am lost, so please give me directions to the nearest embassy.
Officer: I understand. I'm Officer Reyes, my location is downtown precinct three, and my phone number is on file — could you tell me your name, and would you like to leave a message with my badge number for reference?
Tourist: I found this item earlier too, and someone's gone missing near the hotel — I lost my passport, wallet, and phone, and someone stole my camera, suitcase, and money, all in the same afternoon, on top of worrying about the coming storm, hurricane, and flood warnings on the news. By the way, where's the restroom — I need toilet paper and I need Wi-Fi to contact my family.

---

# Topic 11: Family & Relatives

**Basic:**
Michael: What kind of person is your grandmother?
Sarah: She's kind. My grandfather is funny.
Michael: How's your mother?
Sarah: My mother is well.
Michael: Do you have a sister?
Sarah: A younger sister and an older sister.
Michael: A brother?
Sarah: An older brother and a younger brother.

**Intermediate:**
Priya: Tell me about your family — your uncle, cousin, and aunt?
Arun: My uncle and cousin live with my aunt nearby. My father and mother are both teachers.
Priya: Forward my greetings to your husband and wife!
Arun: I will, and my son and daughter say hello too.
Priya: Do you own any pets?
Arun: I own a dog and a cat, plus a bird, a fish, a bunny, and even a snake.

**Advanced:**
Sophia: What kind of person is your grandfather, and how's your grandmother doing these days?
David: My grandmother is well, thank you — she's kind, and my grandfather remains as funny as ever, even at his age. My uncle, cousin, and aunt all still live together, actually, which makes holidays chaotic but wonderful.
Sophia: Forward my greetings to your husband and to your wife's side of the family too, if you don't mind — and to your son and daughter as well.
David: I will, thank you. Between my younger sister, older sister, older brother, and younger brother, our house is already full — and that's before counting the pets. I own a dog, a cat, a bird, a fish, a bunny, and, believe it or not, a snake too.

---

# Topic 12: Your Feelings

**Basic:**
David: How are you doing?
Sophia: I'm happy.
David: Are you tired?
Sophia: I'm not tired. I'm anxious.
David: Were you bored yesterday?
Sophia: Yes, and a little sad.

**Intermediate:**
Priya: How are you doing today?
Arun: I'm angry, honestly, and a bit anxious about the exam.
Priya: Were you embarrassed at the meeting yesterday?
Arun: A little, but I feel great today — active, calm, and hopeful.
Priya: Not disgusted or horrified anymore?
Arun: No, I'm actually proud, relaxed, and satisfied with how it turned out.

**Advanced:**
Sarah: You look tired.
John: I'm not tired — I'm anxious today, honestly, and a little disgusted by the news this morning, though I was in love with the project by lunchtime, if that makes sense.
Sarah: I know the feeling. I'm annoyed and confused about the schedule myself, and frankly exhausted, frightened, and frustrated by how miserable this week has been — nervous doesn't even cover it.
John: It sounds silly, but I feel terrible, upset, and worried too, even though I'm super happy about the weekend and excited for tomorrow. I'm angry at him for canceling, annoyed by what's happened, and I was embarrassed yesterday — yet somehow I'm feeling great today, even if I'm still a little frustrated and lonely underneath it all.

---

# Topic 13: Geography in English

**Basic:**
Teacher: Can you find South America?
Student: Yes, here.
Teacher: What continent is this?
Student: This is Africa.
Teacher: And this?
Student: It's Asia.
Teacher: What's north?
Student: North America.

**Intermediate:**
Arun: Which way is north, south, east, and west from here?
Priya: North is that way, south behind us, east to the right, west to the left.
Arun: Is the store in front or in back, inside or outside?
Priya: In front, inside — it's above the garage, not under it.
Arun: Where do you live, and how many countries have you visited?
Priya: I currently live in Boston, and I've visited ten countries. I really want to visit Japan.

**Advanced:**
David: Can you find the continents on the map — this is South America, next to Central America, with the Atlantic Ocean separating it from Africa and Europe?
Emma: And up north, that's North America, bordered by the Arctic Ocean, while Asia stretches across from Europe, wrapped by the Pacific Ocean on one side and the Indian Ocean near Oceania and Antarctica on the other.
David: I currently live near a coastal city with a beautiful ocean view, a nearby mountain range, and a river that flows past a lake, so I've grown attached to landscapes with a sky full of islands offshore.
Emma: I prefer inland scenery myself — a hill, some mainland forest, a quiet pond, exposed rock, seaside fields, and even the desert, where the soil, dirt, and grass all tell a different story. How many countries have you visited, by the way, and where do you want to visit next — I hear Tokyo, New York, London, New Delhi, Beijing, and Sydney are all worth the trip.

---

# Topic 14: Checking in a Hotel

**Basic:**
Guest: Is there a TV in this hotel?
Staff: Yes, there's a TV.
Guest: Is there an elevator?
Staff: Yes.
Guest: Is there a shower?
Staff: Yes, and a bathtub and a sink.
Guest: I'd like to check in.
Clerk: Sure. Here's your key.

**Intermediate:**
Guest: I'd like to check in. Is there a room service cart available, and does the room have a double-sized bed?
Clerk: Yes, and there's a twin-sized bed option too, plus a vending machine down the corridor near the elevator.
Guest: What time is check out, and is breakfast included?
Clerk: Checkout is at noon, and yes, breakfast is included. Could you please recommend a good restaurant around here, you might ask — I'd suggest the one near the front desk.
Guest: Could you please call me a taxi, and do you have a city guide?
Clerk: Of course, right here.

**Advanced:**
Guest: Are you staying at a hotel, a resort, or did you book a bed & breakfast this time? Personally, I prefer an inn over a full resort, though my brother swears by hostels, and we're even considering a campground for part of the trip.
David: I'd like to check in — does the room have cable TV, air conditioning, a heater, soap, and shampoo, along with a toothbrush and reliable Wi-Fi?
Clerk: All included, and we also have a swimming pool, a gym, and rooms with either a mountain view or an ocean view — everything's all inclusive, and here's your room number and key.
David: Unfortunately, I have a complaint — the Wi-Fi is not working, the bathwater is cold, there're no towels, and it's too noisy on this floor.
Clerk: I sincerely apologize for all of that — let me move you to a quieter room immediately and have someone check the room service cart, king-sized bed suite, and bathroom fixtures right away.

---

# Topic 15: Crush Your Goals — Study Strategy

**Basic:**
Teacher: Why are you learning English?
Student 1: I want to travel to the United States.
Student 2: It's a beautiful language.
Student 3: It's useful for my job.

**Intermediate:**
Teacher: Why are you learning English?
Student 1: I love American culture and people, and it's part of my university studies.
Student 2: I want to speak to my partner's family in English.
Student 3: I live in the United States now, so it's necessary.

**Advanced:**
Teacher: Why are you learning English, really — beyond the obvious reasons?
Student 1: Honestly, it started because I wanted to travel to the United States, but along the way I realized it's simply a beautiful language, and it's become useful for my job in ways I didn't expect.
Student 2: For me, it's more personal — I love American culture and its people, and since I want to speak to my partner's family in English, the motivation runs deeper than any classroom requirement.
Student 3: I live in the United States now, so it stopped being optional, but I also genuinely love learning languages in general — it was part of my university studies originally, and that curiosity never really left.

---

# Topic 16: Question Words, Sentence Patterns & Grammar

**Basic:**
Teacher: Why did you say that?
Student: I don't know why.
Teacher: My name is Adam.
Student: My name is Maria.
Teacher: What time is it? It's 9 o'clock.
Student: I like music.

**Intermediate:**
Priya: Why did you say that, and when did this happen?
Arun: I don't know why, and it happened yesterday, actually.
Priya: Where did it happen, and who told you?
Arun: At the office, and my coworker told me.
Priya: How did you find out, and how much did it cost?
Arun: He mentioned it casually. It cost quite a bit, honestly.
Priya: Which one are you asking about — how much is this dress?
Arun: That one.

**Advanced:**
Teacher: Let's review our question words today — why, what, when, where, who, how, how much, and which — and I want you to build full sentences using our beginner patterns, like "My name is [A]," "What time is it? It's [A] o'clock," "I like [noun]," and "How much is [A]?"
Student: My name is Adam, and it's 9 o'clock — I like music, and honestly, I'd ask how much this dress is before deciding, since I'm not sure which one fits my budget.
Teacher: Good. Now let's also revisit our grammar terms — noun, verb, adjective, particle, adverb, preposition, conjunction, interjection, pronoun, idiom, subject, and object — try using at least three in one sentence about your day.
Student: As the subject of my own sentence, I'd say the pronoun "I" performs the verb "practiced" using an adverb like "carefully," while a preposition connects the object — this idiom-heavy exercise is honestly harder than it looks!

---

# Topic 17: Let's Cook in English

**Basic:**
Sophia: What's in your kitchen?
David: There is a blender and a bowl.
Sophia: A coffeemaker?
David: Yes, and a sink and a cutting board.
Sophia: A timer and a mixer?
David: Yes, and a pot, a microwave oven, a stove, a toaster, and a refrigerator.

**Intermediate:**
Chef: Please add a pinch of salt and black pepper to taste.
Assistant: I need 1 cup of flour and 2 eggs for this recipe.
Chef: Also 200 ml of milk, 300 g of sugar, 3 tablespoons of oil, and 1 teaspoon of butter.
Assistant: Should I stir or slice first?
Chef: Slice the vegetables, then grill, add the seasoning, chop the herbs, peel the fruit, beat the eggs, and mix everything together.

**Advanced:**
Chef: Before we start, please add a pinch of salt and black pepper to taste, and make sure you have 1 cup of flour, 2 eggs, 200 ml of milk, 300 g of sugar, 3 tablespoons of oil, and 1 teaspoon of butter measured out — I need these for this recipe.
Assistant: Understood — should I stir the batter first, or slice the fruit and grill the topping while it rests?
Chef: Slice it thinly, then thinly slice the onions separately, beat the egg whites until stiff, and blend it until smooth before you preheat the oven.
Assistant: And once that's done?
Chef: Add the mixture to the cream, chop what's left of the garnish, peel the remaining fruit, mix it in gently, and let it simmer for 30 minutes — we'll use the blender, bowl, coffeemaker, sink, cutting board, timer, mixer, pot, microwave oven, stove, toaster, and refrigerator before we're finished, so let's stay organized.

---

# Topic 18: Talking About Movies & TV

**Basic:**
Michael: What kind of movies do you like?
Sarah: I like horror movies.
Michael: Comedy or fantasy?
Sarah: Comedy. Also romance and sci-fi.

**Intermediate:**
David: Who's your favorite actor and actress, and do you prefer animation or a dubbed version?
Emma: I like animation, actually, and I always buy a movie ticket for the premiere at the movie theater rather than waiting for the news to cover it.
David: What genre is your favorite episode from, and how many seasons does that series have?
Emma: It's part of a great series, almost a soap opera really, currently on its fourth season, and yes, I watch it with subtitles.
David: How much does the ticket cost, and what time does the movie start?
Emma: Twelve dollars, and it starts at 7 PM — some popcorn, please, while we wait for the trailer.

**Advanced:**
Sophia: What kind of movies do you like — horror, comedy, fantasy, romance, or sci-fi? I ask because I'm trying to figure out whether to buy a movie ticket for tonight's premiere or just wait for it to hit a streaming program later.
Michael: Honestly, I gravitate toward animation and the dubbed version of foreign films rather than reading subtitles, though I'll admit I still watch the news and variety shows out of habit, and I follow at least one soap opera-style series that's now in its fourth season.
Sophia: Do you know how much the ticket costs, and what time the movie starts? I still need to watch the trailer before deciding whether it's worth choosing over a quiet night with a good episode of something else.
Michael: It's twelve dollars, starting at 7 PM — grab some popcorn, please, and let's decide once we're at the movie theater, since my favorite actor is apparently in this one, alongside an actress everyone's been talking about lately.

---

# Topic 19: What's in Your Wallet

**Basic:**
Arun: What's in your wallet?
Priya: There's a debit card and cash.
Arun: A credit card?
Priya: Yes, and my driver's license.

**Intermediate:**
David: Do you carry a wallet, keys, a purse, or your passport with you daily?
Sophia: I carry a wallet, keys, and my passport, plus an umbrella, my cell phone, a wristwatch, and earphones.
David: What's your e-mail address, and where are you from?
Sophia: My e-mail address is maria@email.com, and I'm from Spain.
David: What's your passport number, for the form?
Sophia: My passport number is AB123456.

**Advanced:**
Arun: What's in your wallet these days — I still keep a point card, a business card, an insurance card, and an identification card in mine, alongside the usual debit card, credit card, cash, and driver's license.
Priya: I've simplified mine, though I still always carry glasses, gloves, a briefcase, and a camera for work, along with a purse, my passport, an umbrella, a cell phone, a wristwatch, and earphones.
Arun: This form is asking for everything — name, surname, age, birthday, occupation, address, country, nationality, phone number, and e-mail. It's also asking whether I'm male or female, and single, married, or divorced.
Priya: Same here — my e-mail address is on file already, and my passport number is AB123456, so filling in "where are you from" is the only field I actually have to think about.

---

# Topic 20: Knowing Your Body

**Basic:**
Doctor: This is my ear.
Patient: This is my mouth.
Doctor: Nose and hair?
Patient: Here. Also chin, eye, neck, and tongue.
Doctor: Cheek, lip, forehead, and eyebrow?
Patient: All fine.

**Intermediate:**
Doctor: Does your head hurt, or your shoulder?
Patient: My hand and chest are fine, but my back hurts near my navel.
Doctor: What about your finger, foot, leg, or knee?
Patient: My knee and arm feel stiff, and my ankle is a little swollen.
Doctor: We'll also check your heart, stomach, lung, and brain.
Patient: And my liver, kidney, bladder, and blood, please.

**Advanced:**
Doctor: Let's go through everything systematically — starting with your face: ear, mouth, nose, hair, chin, eye, neck, tongue, cheek, lip, forehead, and eyebrow all look normal so far.
Patient: Good to hear. My head, shoulder, hand, and chest feel fine too, though my back has been bothering me near my navel, and my finger and foot have both been a bit numb lately.
Doctor: We'll want to examine your leg, knee, arm, and ankle as well, then move on to internal organs — heart, stomach, lung, brain, liver, kidney, bladder, and blood work, just to be thorough.
Patient: While we're at it, could you also test my five senses — taste, sight, hearing, touch, and smell? I've noticed some changes recently that I'd rather rule out early.

---

# Topic 21: National Holidays

**Basic:**
Woman: What are you doing for Fourth of July?
Man: I'm planning to go to the beach.
Woman: Staying home?
Man: No, but sometimes I go to the movies or go camping.

**Intermediate:**
Sophia: Do you celebrate Columbus Day and Inauguration Day?
David: Yes, and Independence Day, Labor Day, and Memorial Day too.
Sophia: What about Thanksgiving Day?
David: Of course. We also visit a church, a mosque, a palace, or a temple sometimes on holidays, with a guidebook in hand.
Sophia: Do you buy a ticket and take a tour bus, or hire a tour guide?
David: Usually a tour guide, since we're tourists in most of these places.

**Advanced:**
Emma: Before I even think about Columbus Day, Inauguration Day, Independence Day, Labor Day, Memorial Day, or Thanksgiving Day travel, I always choose a destination and request vacation time months in advance.
Michael: Same here — I buy a guidebook, save money, and apply for a passport early, since booking a flight and accommodations gets expensive closer to the holiday.
Emma: Once I've packed, bought travel insurance, and gotten a visa if needed, I usually plan visits to a church, mosque, palace, or temple, along with the standard tourist checklist — ticket, tour, tour bus, and tour guide included.
Michael: This year, though, I'm planning to just go to the beach for Fourth of July instead — no temples, no tour guide, just staying home half the time and maybe going camping or to the movies the rest.

---

# Topic 22: Talking About Numbers

**Basic:**
Teacher: What's your favorite number?
Nana: It's seven.
Teacher: One, two, three, four, five, six, seven, eight, nine, ten.
Student: Got it.

**Intermediate:**
Accountant: We're talking about 100 — one hundred, and 1,000 — one thousand.
Client: What about 10,000 and 100,000?
Accountant: Ten thousand, and one hundred thousand.
Client: How long will it take to calculate?
Accountant: It'll take 10 minutes. By the way, how old are you?
Client: I'm 32 years old.

**Advanced:**
Teacher: Today we'll cover the math vocabulary — plus, minus, equals, multiply, divide, half, point, and percent — alongside our large numbers: 100, 1,000, 10,000, 100,000, 1,000,000, 1,000,000,000, and 1,000,000,000,000, meaning one hundred through one trillion.
Student: So if I have one million and I divide it in half, then multiply by a percent, plus or minus a point here or there, I should land somewhere between one thousand and ten thousand, roughly?
Teacher: Exactly the kind of thinking we want. Now, practical questions — how long will it take you to finish this exercise, and separately, how old are you, out of curiosity?
Student: It'll take 10 minutes, probably, and I'm 32 years old — old enough to still get large numbers wrong under pressure, apparently, but my favorite number remains seven.

---

# Topic 23: Occupations

**Basic:**
Interviewer: What do you do?
Candidate 1: I'm an artist.
Candidate 2: I'm a chef.
Candidate 3: I'm a doctor.
Candidate 4: I'm a teacher.

**Intermediate:**
Sophia: Are you a salaried employee, or a freelancer?
Michael: A freelancer, actually. My brother is a temporary worker, and my sister is unemployed right now. My roommate is a full-time worker, and I also pick up part-time work.
Sophia: What's your workplace like?
Michael: There's a good coworker, tight deadlines, regular meetings, and steady teamwork, though I'm overdue for a vacation.

**Advanced:**
Interviewer: Tell me about yourself, and while you're at it, tell me about your education.
Candidate: I'm a company employee currently, though I trained as an engineer — I'm an artist at heart, honestly, having worked as a construction worker and even a photographer before settling into this field.
Interviewer: Why did you leave your last job, and what are your career goals going forward?
Candidate: I left because I wanted to move from part-time work toward becoming a full-time worker with real advancement, and eventually I'd like to become a supervisor. Along the way I've known accountants, actors, architects, dentists, graphic designers, hairdressers, journalists, judges, lawyers, nurses, pilots, scientists, singers, students, and writers — and every one of them talked about company, coworker, deadline, meeting, project, salary, teamwork, and vacation the same way I do.
Interviewer: That's a broad perspective. Are you a police officer, firefighter, mail carrier, or professor by any chance, given your varied background?
Candidate: None of those, though I respect anyone in those roles — I'm simply a dedicated company employee looking for the next step.

---

# Topic 24: Talk to Your Pets

**Basic:**
Sophia: Do you have any pets?
David: I have a dog.
Sophia: A cat too?
David: Yes, and a hamster.
Sophia: A rabbit?
David: Yes, and a goldfish.

**Intermediate:**
Priya: What breed is your dog?
Arun: A German Shepherd. My neighbor has a Great Dane, and my sister has a Labrador Retriever.
Priya: I love Dalmatians, Pugs, and Bulldogs.
Arun: Don't forget Dachshunds, Yorkshire Terriers, Dobermanns, and Poodles.
Priya: Can your dog do tricks — roll over, sit, jump, or shake?
Arun: Yes! Also fetch, lie down, stay, and play dead.

**Advanced:**
Sophia: Do you have any pets beyond the usual dog or cat? I have an iguana, a tarantula, a mouse, a hamster, a rat, a goldfish, and even a parakeet — plus a rabbit, a ferret, a guinea pig, a cat, and a dog, if you can believe it.
David: My household is more conventional — a German Shepherd, and a Persian cat that acts like she owns the place. I've also known people with Great Danes, Labrador Retrievers, Dalmatians, Pugs, Bulldogs, Dachshunds, Yorkshire Terriers, Dobermanns, and Poodles.
Sophia: Does your dog respond to commands like roll over, sit, jump, shake, fetch, lie down, stay, or play dead?
David: All of them, actually. And speaking of cats, I've admired a friend's Maine Coon cat, Siamese cat, Birman cat, Ragdoll cat, Himalayan cat, Sphynx cat, and American Shorthair cat — though I still need to stock up on a collar, a leash, a vaccine, a toy, dog food, cat food, a birdcage, and a hamster ball before my next pet-store trip.

---

# Topic 25: Making a Phone Call

**Basic:**
Caller: Hello, I'd like to speak with the manager.
Receptionist: Okay, just a moment.
Caller: Is this customer service?
Receptionist: Yes.

**Intermediate:**
Caller: Hello, I'd like to speak with the person in charge.
Receptionist: Okay, just a moment.
Caller: I can't hear you very well.
Receptionist: I'll call you back.
Caller: I'm free tomorrow. Please answer the phone next time.
Receptionist: Please wait a moment. The line is busy right now, actually.

**Advanced:**
Caller: Hello, I'd like to speak with the person in charge — is that the manager, a sales representative, or should I go through customer service instead?
Receptionist: Okay, just a moment — I'll check who's available; someone should be able to help.
Caller: I can't hear you very well — could you try again? I'll call you back if this connection keeps dropping.
Receptionist: Understood. Please wait a moment while I transfer you — actually, the line is disconnected on his end. What's your phone number, and would you like to leave a message?
Caller: Sure, go ahead and take it down — by the way, I recently switched to a prepaid mobile phone rather than a monthly contract or installment payment plan, so if the network coverage seems off, that's why. I'm busy this week, but I'm free tomorrow if he wants to call regarding the smartphone's service status, or he can just send a text message.

---

# Topic 26: Asking How to Say Something

**Basic:**
Gabriel: How do you say this?
Librarian: It's "parking lot."
Gabriel: How do you say giraffe?
Librarian: Giraffe.

**Intermediate:**
Student: How do you say this word for shop clerk?
Teacher: It's "shop clerk."
Student: What about travel and invasion?
Teacher: Travel and invasion — good words to know.
Student: Can you help me pronounce breakfast and vocabulary?
Teacher: Sure, let's practice negotiation and miscellaneous too.

**Advanced:**
Gabriel: How do you say this? I keep mixing up words like colleague, hawk, rural, begrime, unfortunately, and realm — some of these are genuinely difficult words to pronounce in English.
Librarian: It's "parking lot," by the way, for the word you asked about earlier. As for pronunciation practice, try this tongue twister: Betty Botter bought some butter, but she said the butter's bitter.
Gabriel: That's tough. What about: Peter Piper picked a peck of pickled peppers — a peck of pickled peppers Peter Piper picked? Or how much wood would a woodchuck chuck if a woodchuck could chuck wood?
Librarian: Even harder — try "lesser leather never weathered wetter weather better," or "if two witches would watch two watches, which witch would watch which watch?" And for a real challenge: imagine an imaginary menagerie manager imagining managing an imaginary menagerie.

---

# Topic 27: Romance & Love

**Basic:**
Man: Hi. Can I sit here?
Woman: Hello.
Man: I'm David. Nice to meet you.
Woman: Nice to meet you too.
Man: You are so beautiful.
Woman: Thanks.

**Intermediate:**
Man: Hi. Can I sit here? I'm David.
Woman: Hello, nice to meet you.
Man: May I ask your name? Don't I know you from somewhere?
Woman: What are you doing here?
Man: I would like to meet you again. You are so kind and interesting.
Woman: Thanks. You're pretty handsome and funny yourself.

**Advanced:**
Man: Hi. Can I sit here? I'm David — nice to meet you, and may I ask your name? I have this odd feeling — don't I know you from somewhere?
Woman: Hello, nice to meet you too. What are you doing here, by the way?
Man: Honestly, I would like to meet you again — you are so beautiful, and also cool, lovely, funny, and interesting all at once, which is a rare combination.
Woman: Thanks — you're being awfully generous with the compliments, though I'll admit your smile is beautiful too, you're smart, I like your hairstyle, and you have good taste, so I suppose I'll allow "when do you want to get married" to remain a joke for now, not a real pick-up line.

---

# Topic 28: Singing in English

**Basic:**
Teacher: What musical instrument can you play?
Student 1: I can play the piano.
Student 2: I can play the guitar.
Student 3: I can play the violin.

**Intermediate:**
Priya: What kind of music do you like — pop or house music?
Arun: Techno and funk, mostly. My brother likes hip hop, R&B, rock 'n' roll, and rap.
Priya: What instrument do you play?
Arun: The accordion and the viola, actually. I also dabble with the harp and French horn.
Priya: My favorite song is "Imagine," and my favorite singer is Adele.
Arun: I can sing very well myself. Do you want to go to karaoke?

**Advanced:**
Teacher: Between the piano, guitar, accordion, viola, violin, harp, French horn, cello, clarinet, saxophone, keyboard, and trumpet, which instruments have you actually mastered, rather than just dabbled in?
Student: I can play the piano and the cello competently, though I'm still working on the saxophone. As for taste, I gravitate toward pop, house music, and techno, but I'll admit a soft spot for funk, hip hop, R&B, rock 'n' roll, and rap depending on my mood.
Teacher: My favorite song changes constantly, but my favorite singer has always been consistent — I love music generally, and I can sing very well when I let myself. Do you want to go to karaoke sometime and put the chorus, solo, soprano, alto, tenor, melody, and rhythm to the test?
Student: Absolutely — and speaking of legends, I'd want a set list spanning Elvis Presley, Michael Jackson, Taylor Swift, Lady Gaga, Nicki Minaj, Johnny Cash, Jimi Hendrix, Tupac Shakur, Eminem, and 50 Cent, if the karaoke machine can handle that range.

---

# Topic 29: Sports and Exercise

**Basic:**
Priya: What sports do you like?
Arun: I like baseball.
Priya: Do you like to work out?
Arun: Yes, weightlifting.
Priya: Basketball or soccer?
Arun: Soccer.

**Intermediate:**
Priya: What sports do you like — ice skating, archery, golf?
Arun: I like weightlifting and track and field. Do you like to work out too?
Priya: Yes, and I go to the gym regularly. I'm a gym member, and I always start with a warm-up exercise and stretching before I sweat.
Arun: Do you use a towel and shower after?
Priya: Of course, and I check the scale to see if I'm trying to gain weight or lose weight.

**Advanced:**
Priya: Between ice skating, archery, baseball, golf, weightlifting, track and field, bowling, tennis, volleyball, badminton, basketball, and soccer, I honestly can't pick a favorite — though I lean toward tennis and volleyball when I actually want a workout.
Arun: I'm more of a gym person myself — being a gym member means I always start with a warm-up exercise and some stretching before I sweat, then finish with a towel and shower, checking the scale afterward depending on whether I'm trying to gain weight or lose weight that month.
Priya: For cardio, do you prefer walking, running, the treadmill, or the bike, and do you mix in jumping, weight training, yoga, or pilates?
Arun: All of the above, plus dancing and strength training on the machine with dumbbells — mostly targeting my abs, arms, shoulders, chest, knees, back, thighs, and calves. And when I actually compete, I'm proud to say I've taken first place and a gold medal twice, though second place with a silver medal, or even third place with a bronze medal, still beats being the loser rather than the winner.

---

# Topic 30: At the Supermarket

**Basic:**
Clerk: What are you looking for?
Sophia: I'm looking for the bakery.
Clerk: The frozen section?
Sophia: Yes, and produce.

**Intermediate:**
Clerk: What are you looking for?
Sophia: I'm looking for a shopping basket, and the frozen section.
Clerk: Anything else — the bakery, beverages, baking supplies?
Sophia: Yes, and condiments, the deli, produce, canned food, and snacks.
Clerk: Need apples, oranges, or strawberries from produce?
Sophia: Yes, and watermelons, bananas, pineapples, cabbage, mushrooms, onions, potatoes, cucumbers, and carrots.

**Advanced:**
Clerk: What are you looking for today — a shopping basket or a shopping cart? Given your list, I'd guess the cart, since you'll need the frozen section, the bakery, beverages, baking supplies, condiments, the deli, produce, canned food, snacks, and the floral department.
Sophia: Exactly right — for produce I need apples, oranges, strawberries, watermelons, bananas, pineapples, cabbage, mushrooms, onions, potatoes, cucumbers, and carrots, and I'll be checking the price, date processed, use by date, and calories on everything.
Clerk: Good habit — also worth checking if it's organic, reading the nutrition facts, confirming it's gluten free, and noting the weight before you buy.
Sophia: There's a sale today too, isn't there — 20% off, or $3 off, and I heard it's buy one, get one free on some items, which will definitely save money whether I pay with cash, a credit card, or a debit card.

---

# Topic 31: Talking Online

**Basic:**
Sophia: I'd like to buy a smartphone.
Clerk: Do you need a laptop too?
Sophia: Yes, and a mouse and keyboard.

**Intermediate:**
Sophia: I'd like to buy a smartphone, a laptop, and a router.
Clerk: Do you need a flash drive or an optical drive too?
Sophia: Yes, and a mouse, keyboard, monitor, and tablet.
Clerk: What about a webcam or computer case?
Sophia: Both, please, along with a sound card.

**Advanced:**
Sophia: I'd like to buy a smartphone, but do you have a flash drive, router, optical drive, mouse, laptop, sound card, keyboard, monitor, tablet, webcam, and computer case in stock as well? I'm essentially rebuilding my whole setup.
Clerk: We do. And once you're set up, are you the type to search for images, click every link, and like or share posts, or do you mostly just upload and download files and follow a few hashtags?
Sophia: A bit of both — lol, brb, btw, lmk, g2g, DM, TBH, IMO — I use all of that shorthand constantly. What's your favorite website, by the way? Are you on Facebook, Instagram, Snapchat, WhatsApp, Skype, Pinterest, LinkedIn, or Twitter?
Clerk: Mostly Instagram and LinkedIn. Please like my photo if you follow me, and feel free to share this if you like it — just let me know how to upload this picture or download that file if you need help once everything's connected.

---

# Topic 32: Planning Your Time

**Basic:**
Sophia: What day is it?
David: It's Sunday.
Sophia: Do you have plans on Monday?
David: No.

**Intermediate:**
Sophia: What day is it, and do you have any plans on Tuesday or Wednesday?
David: It's Monday, and no plans yet. What time is it — the hour and minute?
Sophia: It's 10 AM, almost noon, not PM yet.
David: I love spring and summer, and my sister loves autumn/fall and winter.
Sophia: What time, day of the week, and season is it — this month or next month?

**Advanced:**
Sophia: What day is it — and more importantly, do you have any plans on Thursday, Friday, or Saturday? I'm trying to plan around this week versus next week without stepping on last week's leftover tasks.
David: It's Wednesday, and I'm free. Let's meet at 5 o'clock — 5 minutes before or 5 minutes after works fine, and we can grab coffee for about 5 hours if the conversation runs long.
Sophia: Sounds good. Between January, February, March, April, May, June, July, August, September, October, November, and December, which month works best for the bigger trip — and do you prefer spring, summer, autumn/fall, or winter travel?
David: I'd lean toward spring or autumn/fall, honestly, sometime this year rather than next year, since last year's vacation got completely swallowed by holiday scheduling and I don't want that mistake to repeat itself this weekend or the next.

---

# Topic 33: Travel to the USA

**Basic:**
Tourist: Is there an ATM nearby?
Local: Yes, right there.
Tourist: How much is this?
Vendor: Ten dollars.
Tourist: Where's the station?
Local: Down the street.

**Intermediate:**
Tourist: Is there an ATM nearby? How much is this — twenty dollars?
Vendor: Yes, or ten dollars, five dollars, two dollars, or one dollar for the smaller items.
Tourist: I'd like to go to Times Square. Where's that?
Guide: New York City. Please take me to the hotel afterward, if you don't mind.
Driver: Sure. Where's the restroom, you asked earlier — just down the hall.
Tourist: A hamburger, please. What do you recommend?
Vendor: Try the hot dog or the Smithfield ham.

**Advanced:**
Tourist: Is there an ATM nearby? I need to break a one hundred dollar bill into fifties, twenties, tens, fives, twos, and ones, plus some change — half dollar and quarter dollar coins if you have them.
Local: There's one near Faneuil Hall Marketplace in Boston, Massachusetts, actually — not far from Navy Pier in Chicago, Illinois, if you happen to be traveling that way too, or Fisherman's Wharf out in San Francisco.
Tourist: I'd like to go to Times Square and Disneyland Park eventually, but first — do you understand me? I don't understand this menu at all, and I don't speak much English.
Local: I understand you fine — do you speak English? Yes, I do, though I don't understand every regional phrase myself. Can you eat this? Of course, unless you can't eat it for allergy reasons.
Tourist: A hamburger, please, and what do you recommend besides that — the hot dog, Smithfield ham, Boston baked beans, bacon and eggs, apple pie, grits, or Jelly Belly Candy? One, two, three, four, five, six, seven, eight, nine, ten — I'll take ten of the candy, please, hello, excuse me, and thank you for your patience; I'm sorry for all the questions, but nice to meet you, and yes, I appreciate the help.

---

# Topic 34: Useful Verbs

**Basic:**
David: I like to cook.
Emma: I like to eat.
David: Do you drink coffee?
Emma: Yes, and I listen to music.

**Intermediate:**
David: I like to cook and drink coffee in the morning.
Emma: I prefer to eat breakfast while I listen to music.
David: Do you nap or shop in the afternoon?
Emma: Sometimes I nap, and I love to shop, sightsee, and sing.
David: What are you doing right now?
Emma: I'm watching TV. What will you do tomorrow?
David: I will travel. What did you do yesterday?
Emma: I went to the cinema.

**Advanced:**
David: I like to cook, drink coffee, eat breakfast, and listen to music most mornings, though I'll occasionally nap, shop, sightsee, or sing if the day allows for it — otherwise I stretch, think, wait, and walk just to clear my head.
Emma: Our textbook covers call, can, come, cut, do, go, help, make, return, ride, see, and use — could you use three of those in one sentence about your week?
David: Sure — I'll call a friend, go help her move, and return the favor by riding along afterward. As for language study, I ask, hear, read, and speak daily, though I only study, teach, understand, and write when I'm feeling ambitious.
Emma: Good discipline. And remember our opposites — open versus close, begin versus finish, push versus pull, turn on versus turn off — try weaving a few into your next journal entry. What are you doing right now, by the way, what will you do tomorrow, and what did you do yesterday?
David: Right now, I'm watching TV; tomorrow, I will travel; and yesterday, I went to the cinema — proof that even simple verbs can carry a whole week's story.

---

# Topic 35: How's the Weather?

**Basic:**
Sophia: How's the weather?
David: It's sunny.
Sophia: Was it rainy yesterday?
David: Yes, and windy.

**Intermediate:**
Sophia: How's the weather today — sunny or cloudy?
David: Sunny, though yesterday it was cloudy and rainy, then windy, misty, and snowy by evening.
Sophia: Is it hot or cold now?
David: It's hot now, though this morning it was warm, then cool, and clear before that.
Sophia: Should I bring a hat, gloves, and sunscreen, or an umbrella and raincoat?
David: Bring both — also sunglasses, a scarf, and rain boots, just in case.

**Advanced:**
Sophia: How's the weather looking for the trip — sunny, cloudy, rainy, windy, misty, snowy, stormy, or clear? The forecast keeps flipping between hot, warm, cool, and cold depending on the hour.
David: They're predicting a blizzard early on, with cloud cover, fog, and unexpected heat later — plus a hurricane warning, some ice, lightning, and mist scattered through the week, followed by rain, a shower, snow, a storm, sun, thunder, a possible tornado, and steady wind, which is a lot for one forecast.
Sophia: Given all that, I'll pack a hat, gloves, a parasol, rain boots, a raincoat, a scarf, sunglasses, sunscreen, and an umbrella, and I'll keep checking the temperature — in degrees Celsius and Fahrenheit — to see whether we're above zero or below zero before we leave.
David: Good call. What's it like outside right now, and what's the actual weather forecast say — it seems like it's going to rain, based on the temperature drop.

---

# Topic 36: Leisure Time Activities

**Basic:**
Sophia: What do you like to do in your free time?
David: I like to read.
Sophia: I like to paint.
David: Do you watch movies?
Sophia: Yes, and I ride a bike.

**Intermediate:**
Sophia: What do you like to do in your free time — play video games or take pictures?
David: I like to surf the internet and paint. What about you?
Sophia: I like to read, watch movies, and play an instrument.
David: I prefer to ride a bike, listen to music, go shopping, go camping, and play sports.
Sophia: What are you doing this weekend?
David: I'm going to go to the movies, or maybe go to a concert.

**Advanced:**
Sophia: In your free time, do you lean toward playing video games, taking pictures, surfing the internet, painting, reading, watching movies, playing an instrument, riding a bike, listening to music, going shopping, going camping, or playing sports — or some combination of all of it?
David: Mostly reading and watching movies, honestly — I love comedy and science fiction, but I'll happily sit through romance, thriller, action, fantasy, musical, horror, drama, documentary, animation, or mystery if the reviews are good.
Sophia: What are you doing this weekend, then — going to the movies, going to a concert, hitting an amusement park, going to karaoke, staying home to watch TV, going to the park, going to the theater, or traveling somewhere new?
David: Probably going to the movies — I still haven't seen "The Sixth Sense," "Aliens," "Blackfish," "The Nightmare Before Christmas," or "The Princess Bride," so it's officially time to catch up on some must-see American movies.
"""

def parse_text(text):
    raw_topics = text.strip().split("# Topic ")
    topics_data = []
    
    for raw in raw_topics:
        if not raw.strip():
            continue
        lines = raw.strip().split('\n')
        header_line = lines[0] # e.g. "1: Useful Adjectives"
        topic_num_and_title = header_line.split(":", 1)
        topic_num = int(topic_num_and_title[0].strip())
        topic_title = topic_num_and_title[1].strip()
        
        # parse sections
        content = "\n".join(lines[1:])
        sections = re.split(r'\*\*(Basic|Intermediate|Advanced):\*\*', content)
        
        levels = {}
        for i in range(1, len(sections), 2):
            lvl_name = sections[i].strip() # "Basic", "Intermediate", "Advanced"
            lvl_text = sections[i+1].strip()
            
            # parse dialogue lines
            dlg_lines = []
            speakers_seen = {}
            
            for line in lvl_text.split('\n'):
                line = line.strip()
                if not line or line.startswith("---"):
                    continue
                if ":" in line:
                    parts = line.split(":", 1)
                    speaker = parts[0].strip()
                    dlg_text = parts[1].strip()
                    
                    if speaker not in speakers_seen:
                        speakers_seen[speaker] = "A" if len(speakers_seen) == 0 else "B"
                    
                    role = speakers_seen[speaker]
                    dlg_lines.append({
                        "speaker": speaker,
                        "text": dlg_text,
                        "role": role
                    })
            levels[lvl_name] = dlg_lines
            
        topics_data.append({
            "topic_num": topic_num,
            "title": topic_title,
            "basic": levels.get("Basic", []),
            "intermediate": levels.get("Intermediate", []),
            "advanced": levels.get("Advanced", [])
        })
    return topics_data

topics = parse_text(prompt_text)

# Build existing 5 topics
existing_5 = [
    {
        "id": 1,
        "title": "Ordering Hot Coffee",
        "scenario": "In a bustling downtown coffee shop, Sarah is ordering a morning beverage from a friendly barista named Alex.",
        "basic": [
            {"speaker": "Alex", "text": "Good morning! Welcome to Brew Haven. What can I get for you today?", "role": "A"},
            {"speaker": "Sarah", "text": "Hi! I'd like a small coffee, please.", "role": "B"},
            {"speaker": "Alex", "text": "Hot or iced?", "role": "A"},
            {"speaker": "Sarah", "text": "Hot, please. With milk.", "role": "B"},
            {"speaker": "Alex", "text": "That will be three dollars.", "role": "A"},
            {"speaker": "Sarah", "text": "Here you go. Thank you!", "role": "B"}
        ],
        "intermediate": [
            {"speaker": "Alex", "text": "Good morning! Welcome to Brew Haven. What can I get started for you today?", "role": "A"},
            {"speaker": "Sarah", "text": "Hi! Can I please get a medium vanilla latte with oat milk?", "role": "B"},
            {"speaker": "Alex", "text": "You got it! Hot or iced?", "role": "A"},
            {"speaker": "Sarah", "text": "Hot, please. And could I also get one of those chocolate croissants in the display case?", "role": "B"},
            {"speaker": "Alex", "text": "Sure thing! Do you want that warmed up?", "role": "A"},
            {"speaker": "Sarah", "text": "Yes, please! That would be lovely.", "role": "B"},
            {"speaker": "Alex", "text": "Perfect. Your total is eight dollars and fifty cents. Tap your card whenever you are ready.", "role": "A"}
        ],
        "advanced": [
            {"speaker": "Alex", "text": "Welcome back to Brew Haven! Shall I set you up with your usual single-origin pour-over today?", "role": "A"},
            {"speaker": "Sarah", "text": "Actually, I'd love to try your seasonal dark roast espresso with a splash of oat milk and half-sweet vanilla syrup.", "role": "B"},
            {"speaker": "Alex", "text": "Excellent choice! Would you care to pair that with a freshly baked almond croissant toasted to perfection?", "role": "A"},
            {"speaker": "Sarah", "text": "That sounds delightful. Could you also pack a double espresso in a travel mug for my meeting downtown?", "role": "B"},
            {"speaker": "Alex", "text": "Absolutely! I'll prepare both right away. Your total comes to twelve dollars and seventy-five cents.", "role": "A"},
            {"speaker": "Sarah", "text": "Perfect, tapping my card now. Thanks as always, Alex!", "role": "B"}
        ],
        "vocabularyCallout": "Latte (espresso with milk), Oat Milk (dairy alternative), Display Case (glass container showing food)",
        "comprehensionQuestion": "What kind of milk did Sarah request for her latte?",
        "comprehensionOptions": "Whole Milk,Almond Milk,Oat Milk,Soy Milk",
        "comprehensionAnswer": "Oat Milk"
    },
    {
        "id": 2,
        "title": "The Job Interview",
        "scenario": "A software engineer named David is interviewing with a hiring manager, Elena, for a Senior Mobile Developer position.",
        "basic": [
            {"speaker": "Elena", "text": "Hello David, nice to meet you. Please take a seat.", "role": "A"},
            {"speaker": "David", "text": "Thank you, Elena. I'm excited to be here today.", "role": "B"},
            {"speaker": "Elena", "text": "Can you tell me about your work experience?", "role": "A"},
            {"speaker": "David", "text": "I am a mobile app developer. I have worked on Android apps for five years.", "role": "B"},
            {"speaker": "Elena", "text": "That sounds great. Why do you want to join our company?", "role": "A"},
            {"speaker": "David", "text": "Your team builds awesome products, and I want to help create great apps with you.", "role": "B"}
        ],
        "intermediate": [
            {"speaker": "Elena", "text": "Thanks for coming in, David. To start off, could you tell me a bit about your background in mobile development?", "role": "A"},
            {"speaker": "David", "text": "Certainly! I've been developing native Android apps for about five years, primarily focusing on clean architecture and Jetpack Compose.", "role": "B"},
            {"speaker": "Elena", "text": "That's great. We use Compose heavily here. How do you handle complex state management in larger projects?", "role": "A"},
            {"speaker": "David", "text": "I prefer using UI State holders with StateFlow, driven by ViewModels, backed by local offline caches using Room.", "role": "B"},
            {"speaker": "Elena", "text": "Impressive answer. What is your strategy for handling tight deadlines or shifting requirements?", "role": "A"},
            {"speaker": "David", "text": "I focus on incremental development and keeping communication open. I'd rather raise risks early than miss a milestone.", "role": "B"}
        ],
        "advanced": [
            {"speaker": "Elena", "text": "Good afternoon, David. Beyond technical fluency, how do you approach architectural design tradeoffs when scaling mobile applications?", "role": "A"},
            {"speaker": "David", "text": "I prioritize modularity and separation of concerns using clean architecture, leveraging reactive flows to decouple state from side effects.", "role": "B"},
            {"speaker": "Elena", "text": "Excellent. When cross-functional requirements conflict under tight quarter deadlines, how do you align engineering priorities with product leadership?", "role": "A"},
            {"speaker": "David", "text": "I establish transparent risk matrices early, propose incremental phased deliverables, and ensure continuous automated testing to maintain quality without sacrificing momentum.", "role": "B"},
            {"speaker": "Elena", "text": "That level of strategic alignment is exactly what our engineering organization needs. Let's discuss your leadership vision for the engineering squad.", "role": "A"}
        ],
        "vocabularyCallout": "StateFlow (reactive stream), Room (SQLite database wrapper), Milestones (key deadlines in project management)",
        "comprehensionQuestion": "How many years of experience does David have in mobile development?",
        "comprehensionOptions": "2 Years,3 Years,5 Years,10 Years",
        "comprehensionAnswer": "5 Years"
    },
    {
        "id": 3,
        "title": "A Friendly Cafe Catch-up",
        "scenario": "Two college friends, Emily and Michael, run into each other at a quiet bookstore cafe after several months.",
        "basic": [
            {"speaker": "Emily", "text": "Hi Michael! Long time no see!", "role": "A"},
            {"speaker": "Michael", "text": "Emily! Wow, good to see you! How are you?", "role": "B"},
            {"speaker": "Emily", "text": "I'm good! I started a new job last week.", "role": "A"},
            {"speaker": "Michael", "text": "Congratulations! What kind of job is it?", "role": "B"},
            {"speaker": "Emily", "text": "I'm a graphic designer now. How about you?", "role": "A"},
            {"speaker": "Michael", "text": "I'm still playing music in my band. We have a show this Friday!", "role": "B"}
        ],
        "intermediate": [
            {"speaker": "Emily", "text": "Oh my gosh, Michael? Is that you? It's been ages!", "role": "A"},
            {"speaker": "Michael", "text": "Emily! Wow, what a surprise! Yes, it's me. How have you been?", "role": "B"},
            {"speaker": "Emily", "text": "I've been good! Busy, but good. I actually started a new graphic design job last month.", "role": "A"},
            {"speaker": "Michael", "text": "No way, congratulations! That's your dream gig! Where is the office located?", "role": "B"},
            {"speaker": "Emily", "text": "It's right in the arts district. How about you? Are you still playing in that indie rock band?", "role": "A"},
            {"speaker": "Michael", "text": "Haha, yes we are! We actually have a gig coming up this Friday night if you're free.", "role": "B"}
        ],
        "advanced": [
            {"speaker": "Emily", "text": "Michael! I can't believe we bumped into each other here! It feels like an eternity since our university days.", "role": "A"},
            {"speaker": "Michael", "text": "Emily, what a wonderful coincidence! You look incredible! How has life been treating you lately?", "role": "B"},
            {"speaker": "Emily", "text": "It's been a whirlwind! I recently transitioned to a lead creative director role at an agency downtown, which keeps me on my toes.", "role": "A"},
            {"speaker": "Michael", "text": "That's outstanding! You always had an incredible vision for visual design. How is the team atmosphere?", "role": "B"},
            {"speaker": "Emily", "text": "Dynamic and fast-paced! Meanwhile, I heard your band just released a vinyl record—that's monumental!", "role": "A"},
            {"speaker": "Michael", "text": "Thanks! It's been a passion project for years. You must join us for our album release party this Friday evening!", "role": "B"}
        ],
        "vocabularyCallout": "It's been ages (a very long time), Gig (a live musical performance or freelance job)",
        "comprehensionQuestion": "What kind of job did Emily recently start?",
        "comprehensionOptions": "Software Developer,Graphic Designer,Barista,Music Teacher",
        "comprehensionAnswer": "Graphic Designer"
    },
    {
        "id": 4,
        "title": "Asking for Airport Directions",
        "scenario": "John is at Heathrow Airport looking for the transit trains to central London and asks an airport assistant, Claire.",
        "basic": [
            {"speaker": "John", "text": "Excuse me, where is the train to the city center?", "role": "A"},
            {"speaker": "Claire", "text": "Go straight down this hallway and take the stairs down.", "role": "B"},
            {"speaker": "John", "text": "Do I buy a ticket here?", "role": "A"},
            {"speaker": "Claire", "text": "Yes, at the ticket machine near the entrance.", "role": "B"},
            {"speaker": "John", "text": "Thank you very much!", "role": "A"},
            {"speaker": "Claire", "text": "You're welcome! Have a good trip!", "role": "B"}
        ],
        "intermediate": [
            {"speaker": "John", "text": "Excuse me, sorry to bother you, but could you tell me where the express train station is?", "role": "A"},
            {"speaker": "Claire", "text": "No bother at all! You'll want to head straight down this corridor, then take the escalators down to Level B1.", "role": "B"},
            {"speaker": "John", "text": "Okay, down to B1. Do I need to buy a ticket beforehand, or can I purchase one on the train?", "role": "A"},
            {"speaker": "Claire", "text": "You must buy a ticket before boarding. There are ticket kiosks right next to the train gates, or you can tap with a contactless bank card.", "role": "B"},
            {"speaker": "John", "text": "Oh, that's perfect! Contactless makes it easy. Thank you so much for your help!", "role": "A"},
            {"speaker": "Claire", "text": "You are very welcome. Have a safe and pleasant journey to London!", "role": "B"}
        ],
        "advanced": [
            {"speaker": "John", "text": "Good morning. Could you kindly direct me to the high-speed rail terminal for downtown transfers?", "role": "A"},
            {"speaker": "Claire", "text": "Certainly! Proceed straight through this main concourse, take the high-speed escalators down to concourse level B1, and follow the blue signage.", "role": "B"},
            {"speaker": "John", "text": "Perfect. Are international transit passes valid on this line, or should I procure a separate fare ticket at the customer kiosk?", "role": "A"},
            {"speaker": "Claire", "text": "Contactless credit card payments and mobile wallet passes are accepted directly at the automated turnstiles for seamless entry.", "role": "B"},
            {"speaker": "John", "text": "That saves me a tremendous amount of time before my connecting transfer. I appreciate your thorough guidance!", "role": "A"},
            {"speaker": "Claire", "text": "It's my absolute pleasure. Wish you a smooth and effortless transit into the city center!", "role": "B"}
        ],
        "vocabularyCallout": "Corridor (hallway), Escalators (moving staircases), Contactless (paying by tapping cards/phones)",
        "comprehensionQuestion": "Where is the express train station located?",
        "comprehensionOptions": "Terminal 2,Level B1,Next to baggage claim,Ground level",
        "comprehensionAnswer": "Level B1"
    },
    {
        "id": 5,
        "title": "At the Doctor's Clinic",
        "scenario": "Liam is explaining his seasonal allergy symptoms to Dr. Susan during a routine check-up.",
        "basic": [
            {"speaker": "Dr. Susan", "text": "Hello Liam. How are you feeling today?", "role": "A"},
            {"speaker": "Liam", "text": "Hello Doctor. I have a cold and my throat hurts.", "role": "B"},
            {"speaker": "Dr. Susan", "text": "Do you have a fever or a cough?", "role": "A"},
            {"speaker": "Liam", "text": "A little cough, but no fever.", "role": "B"},
            {"speaker": "Dr. Susan", "text": "Rest well and drink plenty of warm water. Here is a prescription for cough syrup.", "role": "A"},
            {"speaker": "Liam", "text": "Thank you Doctor!", "role": "B"}
        ],
        "intermediate": [
            {"speaker": "Dr. Susan", "text": "Welcome back, Liam. What seems to be bringing you in today?", "role": "A"},
            {"speaker": "Liam", "text": "Well, Doctor, my seasonal allergies have been acting up terribly this year. I can't stop sneezing, and my eyes are always itchy.", "role": "B"},
            {"speaker": "Dr. Susan", "text": "I see. Have you been taking any over-the-counter antihistamines?", "role": "A"},
            {"speaker": "Liam", "text": "Yes, but they make me feel incredibly drowsy. I can barely focus at work.", "role": "B"},
            {"speaker": "Dr. Susan", "text": "That's a common side-effect. Let's try switching you to a non-drowsy prescription spray.", "role": "A"},
            {"speaker": "Liam", "text": "That sounds wonderful. I'd love to breathe clearly without falling asleep at my desk!", "role": "B"}
        ],
        "advanced": [
            {"speaker": "Dr. Susan", "text": "Good morning Liam. Let me review your medical history before we discuss your current symptoms. What brings you in today?", "role": "A"},
            {"speaker": "Liam", "text": "Doctor, my chronic allergic rhinitis has escalated significantly this spring, causing severe inflammation, sinus pressure, and persistent fatigue.", "role": "B"},
            {"speaker": "Dr. Susan", "text": "Have standard second-generation oral antihistamines provided any symptomatic relief, or are you experiencing adverse side effects?", "role": "A"},
            {"speaker": "Liam", "text": "Unfortunately, even non-sedating options leave me feeling groggy, which severely impairs my cognitive focus throughout the workday.", "role": "B"},
            {"speaker": "Dr. Susan", "text": "Given those parameters, I recommend initiating a combination corticosteroid nasal spray along with targeted immunotherapy evaluations.", "role": "A"},
            {"speaker": "Liam", "text": "That sounds like a comprehensive and proactive approach. I'm eager to get this under long-term control.", "role": "B"}
        ],
        "vocabularyCallout": "Acting up (experiencing worse symptoms), Antihistamines (allergy medication), Drowsy (feeling sleepy)",
        "comprehensionQuestion": "Why did Liam want to switch his allergy medication?",
        "comprehensionOptions": "It was too expensive,It made him feel sleepy,It didn't work at all,He ran out of pills",
        "comprehensionAnswer": "It made him feel sleepy"
    }
]

# Generate all 41 topics list
all_topics_list = []
for item in existing_5:
    all_topics_list.append(item)

for index, t in enumerate(topics):
    topic_id = len(existing_5) + index + 1
    # Extract keywords for callout and quiz
    basic_lines = t["basic"]
    inter_lines = t["intermediate"]
    adv_lines = t["advanced"]
    
    # Pick a quiz question based on content
    q_text = f"What is the main topic discussed in '{t['title']}'?"
    q_opts = f"Core vocabulary for {t['title']},Unrelated grammar rules,Weather forecasts,Cooking recipes"
    q_ans = f"Core vocabulary for {t['title']}"
    
    # Vocabulary callout
    vocab_callout = f"Basic Expressions, Intermediate Phrases, Advanced Vocabulary for {t['title']}"
    
    all_topics_list.append({
        "id": topic_id,
        "title": f"Topic {t['topic_num']}: {t['title']}",
        "scenario": f"Practice conversational English for '{t['title']}' across Basic, Intermediate, and Advanced levels.",
        "basic": basic_lines,
        "intermediate": inter_lines,
        "advanced": adv_lines,
        "vocabularyCallout": vocab_callout,
        "comprehensionQuestion": q_text,
        "comprehensionOptions": q_opts,
        "comprehensionAnswer": q_ans
    })

print(f"Total topics prepared: {len(all_topics_list)}")

# Now generate the Kotlin code snippet for InitialData.kt!
def escape_str(s):
    return s.replace('\\', '\\\\').replace('"', '\\"')

kotlin_code_lines = []
kotlin_code_lines.append("    val conversations = listOf(")

for idx, top in enumerate(all_topics_list):
    b_json = json.dumps(top["basic"], ensure_ascii=False)
    i_json = json.dumps(top["intermediate"], ensure_ascii=False)
    a_json = json.dumps(top["advanced"], ensure_ascii=False)
    
    # Fallback json defaults to basic or intermediate
    default_json = i_json if len(top["intermediate"]) > 0 else b_json
    
    comma = "," if idx < len(all_topics_list) - 1 else ""
    
    entry = f"""        ConversationSet(
            id = {top['id']},
            title = "{escape_str(top['title'])}",
            scenario = "{escape_str(top['scenario'])}",
            basicDialogueJson = \"\"\"{b_json}\"\"\".trimIndent(),
            intermediateDialogueJson = \"\"\"{i_json}\"\"\".trimIndent(),
            advancedDialogueJson = \"\"\"{a_json}\"\"\".trimIndent(),
            dialogueJson = \"\"\"{default_json}\"\"\".trimIndent(),
            vocabularyCallout = "{escape_str(top['vocabularyCallout'])}",
            comprehensionQuestion = "{escape_str(top['comprehensionQuestion'])}",
            comprehensionOptions = "{escape_str(top['comprehensionOptions'])}",
            comprehensionAnswer = "{escape_str(top['comprehensionAnswer'])}"
        ){comma}"""
    kotlin_code_lines.append(entry)

kotlin_code_lines.append("    )")

full_kotlin_code = "\n".join(kotlin_code_lines)

with open("/app/applet/conversations_generated.kt", "w") as f:
    f.write(full_kotlin_code)

print("Generated conversations_generated.kt successfully!")
