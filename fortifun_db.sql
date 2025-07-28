-- Use the created database
USE fortifund_db;

-- Table for general text content (paragraphs, titles, descriptions)
CREATE TABLE IF NOT EXISTS text_content (
    id INT AUTO_INCREMENT PRIMARY KEY,
    section_name VARCHAR(255) NOT NULL, -- e.g., 'home_hero', 'why_fortifund_section'
    content_key VARCHAR(255) NOT NULL,  -- e.g., 'hero_title', 'hero_description', 'section_heading'
    content_value TEXT NOT NULL,
    UNIQUE (section_name, content_key)
);

-- Table for image assets
CREATE TABLE IF NOT EXISTS image_assets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    section_name VARCHAR(255) NOT NULL, -- e.g., 'home_hero'
    asset_key VARCHAR(255) NOT NULL,    -- e.g., 'hero_image'
    image_url VARCHAR(2048) NOT NULL,
    alt_text VARCHAR(512),
    UNIQUE (section_name, asset_key)
);

-- Table for video assets
CREATE TABLE IF NOT EXISTS video_assets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    section_name VARCHAR(255) NOT NULL, -- e.g., 'charting_course_section'
    asset_key VARCHAR(255) NOT NULL,    -- e.g., 'charting_video'
    video_url VARCHAR(2048) NOT NULL,
    poster_url VARCHAR(2048),           -- Optional poster image for video
    UNIQUE (section_name, asset_key)
);

-- Table for FAQ items
CREATE TABLE IF NOT EXISTS faq_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    display_order INT NOT NULL UNIQUE -- To maintain order of FAQs
);

-- Table for navigation bar items
CREATE TABLE IF NOT EXISTS navbar_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_text VARCHAR(255) NOT NULL,
    item_href VARCHAR(255) NOT NULL,
    is_dropdown BOOLEAN DEFAULT FALSE,
    parent_id INT, -- For dropdown items, links to parent navbar_item
    display_order INT NOT NULL,
    UNIQUE (item_text, item_href),
    FOREIGN KEY (parent_id) REFERENCES navbar_items(id) ON DELETE CASCADE
);

-- Table for contact form submissions
CREATE TABLE IF NOT EXISTS contact_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for book demo form submissions
CREATE TABLE IF NOT EXISTS demo_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    company VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    comments TEXT,
    demo_date DATE,
    demo_time TIME,
    submission_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Initial Data for text_content (Comprehensive)
INSERT INTO text_content (section_name, content_key, content_value) VALUES
('home_hero', 'hero_title', 'De-Risking <br> Human Progress'),
('home_hero', 'hero_description', 'With elite insurance expertise empowered by breakthrough technology, FortiFund is the modern insurance brokerage for the 21st century.'),

('alt_financing_section', 'section_heading', 'De-Risking Alternative Financing'),
('alt_financing_section', 'section_description_large', 'The alternative finance space is fast-moving, fragmented, and full of risk, but it doesn\'t have to be.'),
('alt_financing_section', 'section_description_normal', 'FortiFund was built by experts who know the game inside and out. We help brokerages reduce risk at every step.'),

('our_approach_section', 'section_heading', 'Our Approach'),
('our_approach_section', 'step1_title', 'Know the players'),
('our_approach_section', 'step1_description', 'We maintain strong relationships with a variety of non-traditional capital sources with appetites for financing corporate risk.'),
('our_approach_section', 'step2_title', 'Outline the possibilities'),
('our_approach_section', 'step2_description', 'We deeply understand the opportunities and propose alternative risk financing solutions aligned to risk management needs.'),
('our_approach_section', 'step3_title', 'Analyze the options'),
('our_approach_section', 'step3_description', 'We quantify both fixed and frictional costs, articulate funding requirements, and outline structural opportunities.'),
('our_approach_section', 'step4_title', 'Propose optimal strategy'),
('our_approach_section', 'step4_description', 'We make a recommendation and lay out the best possible structure alternatives, tailored to your business needs.'),

('why_fortifund_section', 'section_heading', 'Why FortiFund?'),
('why_fortifund_section', 'why_precision_matching_title', 'Precision Matching:'),
('why_fortifund_section', 'why_precision_matching_description', 'Send your deal to best-fit lenders. Long gone are the days of auto declines due to restrictions. FortiFund makes it instant, confidential, and smarter.'),
('why_fortifund_section', 'why_higher_commissions_title', 'Higher Commissions:'),
('why_fortifund_section', 'why_higher_commissions_description', 'Prioritize lenders by who pays you the most, or the highest dollar amount funded.'),
('why_fortifund_section', 'why_backdoor_protection_title', 'Backdoor Protection:'),
('why_fortifund_section', 'why_backdoor_protection_description', 'Take the first step to taking control of your deals. Send your deals with confidence.'),
('why_fortifund_section', 'why_save_hours_title', 'Save Hours:'),
('why_fortifund_section', 'why_save_hours_description', 'Submit deals in minutes – no spreadsheets, no guesswork.'),

('by_the_numbers_section', 'section_heading', 'By the Numbers'),
('by_the_numbers_section', 'section_description', 'The data speaks for itself. From our large roster of established and growing clients to our stellar client retention rate—we build relationships that last.'),
('by_the_numbers_section', 'metric1_value', '$3.1B'),
('by_the_numbers_section', 'metric1_description', 'in annual premiums placed'),
('by_the_numbers_section', 'metric2_value', '~20%'),
('by_the_numbers_section', 'metric2_description', 'U.S. unicorns represented'),
('by_the_numbers_section', 'metric3_value', '500+'),
('by_the_numbers_section', 'metric3_description', 'public company experience'),

('results_section', 'section_heading', 'The Results Speak for Themselves'),
('results_section', 'results_description', 'The numbers speak for themselves. On average, our brokers are seeing:'),
('results_section', 'result1_text', '+36% higher offer amounts'),
('results_section', 'result2_text', '+57% increase in funded amounts'),
('results_section', 'result3_text', 'Fewer declined deals, fewer wasted submissions'),
('results_section', 'client_trust_heading', 'Clients Trust FortiFund'),
('results_section', 'client_trust_description', 'Our experts consult with companies across all growth stages on strategies that align with their benefits philosophy.'),

('not_crm_section', 'section_heading', 'We are not a CRM...'),
('not_crm_section', 'section_description', 'Unlike a CRM, FortiFund doesn\'t just store contacts - it matches your deals to the right lenders in real time. No manual filtering, no guesswork - just action.'),

('testimonials_section', 'section_heading', 'What Brokers Are Saying'),
('testimonials_section', 'testimonial1_text', '"FortiFund changed how we work. Submissions are faster, matches are more accurate, and close rates are way up."'),
('testimonials_section', 'testimonial1_author', '- Maria T., Managing Broker'),
('testimonials_section', 'testimonial2_text', '"It feels like FortiFund gets how this business really works. I send to one lender, not ten, and I get funded faster."'),
('testimonials_section', 'testimonial2_author', '- DeShawn B., Commercial Funding Agent'),

('charting_course_section', 'section_tagline', 'IMPACTFUL INNOVATION'),
('charting_course_section', 'section_heading', 'Charting a New Course'),
('charting_course_section', 'section_description', 'We\'re bringing advanced technology to an antiquated industry fostering transparency, convenience, and optimized client outcomes.'),
('charting_course_section', 'charting_bullet1', 'Business insurance clients can have 24/7 access to their entire insurance program including policies, losses, COs, and billing, on any device through FortiFund\'s connected dashboard.'),
('charting_course_section', 'charting_bullet2', 'Total rewards clients can access benefit plans, compliance information, and secure documents in our centralized platform.'),
('charting_course_section', 'charting_bullet3', 'Predictive analytics and proprietary benchmarking enable better carrier negotiations and informed decision-making.'),
('charting_course_section', 'charting_bullet4', 'Multiple AI-enabled technology solutions continue to be developed, saving clients time and improving their experiences.'),

('how_it_works_section', 'section_heading', 'How FortiFund Works'),
('how_it_works_section', 'section_description', 'Skip the guesswork. FortiFund makes deal matching effortless from intake to funding. (add demo video)'),
('how_it_works_section', 'step1_title', 'Step 1: Submit Your Deal'),
('how_it_works_section', 'step1_description', 'Fill out a short form and upload your documents – no spreadsheets, no back-and-forth.'),
('how_it_works_section', 'step2_title', 'Step 2: Get a Precision Match'),
('how_it_works_section', 'step2_description', 'We instantly analyze your deal against real lender criteria + your preferences to recommend the best-fit lender.'),
('how_it_works_section', 'step3_title', 'Step 3: Send It Yourself (For Now)'),
('how_it_works_section', 'step3_description', 'You send the deal to your matched lender manually – just like you do today, but with confidence you\'re sending to the right lender.'),
('how_it_works_section', 'step4_title', 'Step 4: Close More Deals'),
('how_it_works_section', 'step4_description', 'By sending fewer, better deals, you reduce friction, increase offer quality, and get funded faster without risking backdoors or delays.'),
('how_it_works_section', 'coming_soon_text', '<strong>Coming Soon:</strong> One-click deal delivery, status tracking, and automatic updates - right inside the FortiFund platform.'),

('faq_section', 'faq_heading', 'Frequently Asked Questions'),

('contact_page', 'contact_heading', 'Contact Us'),
('contact_page', 'contact_email_text', 'Email: <a href="mailto:admin@fortifund.net">admin@fortifund.net</a>'),

('book_demo_page', 'book_demo_heading', 'Book a Demo'),
('book_demo_page', 'book_demo_description', 'Schedule a consultation to see how FortiFund can reduce your risk.'),
('book_demo_page', 'calendar_title', 'Select a Day and Time'),
('book_demo_page', 'calendar_note', 'A full interactive calendar integration would require a dedicated library or API. For this demo, please imagine a calendar widget here.'),

('matchmaking_page', 'matchmaking_title', 'Matchmaking (Live Now)'),
('matchmaking_page', 'matchmaking_description', 'FortiFund’s core matchmaking engine helps brokers instantly connect their deals with the most relevant lenders — based on real funding criteria and your custom preferences.'),
('matchmaking_page', 'matchmaking_subtitle', 'What’s included now:'),
('matchmaking_page', 'matchmaking_feature1_title', 'Precision Matching:'),
('matchmaking_page', 'matchmaking_feature1_description', 'Based on guidelines, commissions, and your preferred lenders.'),
('matchmaking_page', 'matchmaking_feature2_title', 'Manual Deal Sending:'),
('matchmaking_page', 'matchmaking_feature2_description', 'You send the deal after matching — with full control.'),
('matchmaking_page', 'matchmaking_feature3_title', 'Automatic Deal Storage:'),
('matchmaking_page', 'matchmaking_feature3_description', 'Every submitted deal is saved in your dashboard, so you can easily track, revisit, and manage past submissions.'),
('matchmaking_page', 'matchmaking_feature4_title', 'Organized File Uploads:'),
('matchmaking_page', 'matchmaking_feature4_description', 'Deal docs are uploaded and stored per file — no clutter, no confusion.'),
('matchmaking_page', 'matchmaking_tagline', '🎯 Submit once. Match better. Keep everything organized in one place.'),

('upcoming_solutions_page', 'upcoming_title', 'Upcoming Solutions (Launching Soon)'),
('upcoming_solutions_page', 'upcoming_description', 'We’re building even more tools to save time and boost results. Coming soon to FortiFund:'),
('upcoming_solutions_page', 'upcoming_feature1_title', '🧠 AI + OCR File Scanning:'),
('upcoming_solutions_page', 'upcoming_feature1_description', 'Automatically extract key data from your documents to pre-fill intake.'),
('upcoming_solutions_page', 'upcoming_feature2_title', '📂 Drag-and-Drop Uploads with Auto-Sorting:'),
('upcoming_solutions_page', 'upcoming_feature2_description', 'Smarter, smoother upload experience with intelligent file labeling.'),
('upcoming_solutions_page', 'upcoming_feature3_title', '🔔 One-Click Deal Delivery + Status Tracking:'),
('upcoming_solutions_page', 'upcoming_feature3_description', 'Send deals directly to matched lenders and track engagement.'),
('upcoming_solutions_page', 'upcoming_feature4_title', '💬 In-App Chat with Matched Lenders:'),
('upcoming_solutions_page', 'upcoming_feature4_description', 'Communicate without leaving the platform.'),
('upcoming_solutions_page', 'upcoming_tagline', '💡 Want early access to new features? Let us know.');


-- Initial Data for image_assets
INSERT INTO image_assets (section_name, asset_key, image_url, alt_text) VALUES
('home_hero', 'hero_image', 'assets/image/image.png', 'Abstract image representing human progress');

-- Initial Data for video_assets
INSERT INTO video_assets (section_name, asset_key, video_url, poster_url) VALUES
('charting_course_section', 'charting_video', 'assets/video/CAMERA_REEL_LOOP.webm', NULL);

-- Initial Data for FAQ items (already provided by user, included for completeness)
INSERT INTO faq_items (question, answer, display_order) VALUES
('1. What is FortiFund?', 'FortiFund is a deal-matching platform that connects brokered deals to the right lenders based on underwriting criteria automatically, efficiently, and securely. It is not a CRM.', 1),
('2. How is FortiFund different from a CRM?', 'CRMs help manage contacts and communication. FortiFund helps you close more deals by matching submissions with lenders that actually fund your deals without the admin hassle.', 2),
('3. Who can use FortiFund?', 'Brokers and processors looking to submit deals faster and smarter, and lenders searching for deals that meet their funding criteria.', 3),
('4. Can I choose which lenders I work with?', 'Yes! Each brokerage can customize their back end to prioritize preferred lenders including those who offer the highest commissions or who you''ve worked with before.', 4),
('5. How do I submit a deal?', 'Log in, complete the short intake form, and drag-and-drop your files. FortiFund matches your deal with lenders based on both your preferences and lender guidelines.', 5),
('6. Are deals automatically sent to lenders?', 'Not yet, but we''re getting there. Right now, FortiFund matches your deal with the most relevant lenders based on their criteria and your preferences. Once matched, you''ll still send the deal to the lender yourself, just like you normally would but with more confidence that it''s the right fit. We''re actively working on features that will let you send and track deals directly through the platform.', 6),
('7. Can I prioritize lenders based on commission payouts?', 'Yes. FortiFund allows you to rank lenders based on your priorities, including commission structures. Our system takes that into account when suggesting matches.', 7),
('8. How much does it cost to use FortiFund?', 'We offer flexible pricing - pay-per-deal or monthly plans. Contact us for a package tailored to your volume.', 8),
('9. What kind of deals will I see?', 'Only those that match your guidelines. You''ll only see deals worth your time and without any clutter.', 9),
('10. Can I set custom deal preferences?', 'Yes. You can define your underwriting rules, industries, revenue ranges, and more and update them anytime.', 10),
('11. Will I get repeat low-quality deals?', 'No. Brokers only send deals that match your preferences, and we prioritize quality over volume.', 11),
('12. Is my data safe on FortiFund?', 'Yes. Your deal data is stored securely, and sensitive client information is kept private. Only you control who ultimately receives the full details. We''re also building features to improve tracking and data security even further.', 12),
('13. Will my deals be sent to all lenders?', 'No. FortiFund doesn''t blast your deals out. Instead, we use precision matching to identify the single best-fit lender for each deal - based on underwriting criteria, your preferences, and even commission structures. This gives you the confidence to send your deal to just one lender, which: Increases your chance of getting the deal funded, Helps you earn the highest commissions possible, and Eliminates the risk of your deal being backdoored or overexposed. You stay in full control once you receive a match, you send the deal directly, just like you normally would.', 13),
('14. Can I get help tailoring FortiFund to my workflow?', 'Yes! Our team will work with you to configure your dashboard, deal settings, and lender preferences to fit your business goals.', 14),
('15. What if I need help with a hard-to-place deal?', 'We''re here to help. Our team and tools are designed to find the right match even for deals that don''t fit the standard mold.', 15);

-- Initial Data for Navbar Items (already provided by user, included for completeness)
INSERT INTO navbar_items (item_text, item_href, is_dropdown, parent_id, display_order) VALUES
('Home', 'index.html', FALSE, NULL, 1),
('FAQ', 'faq.html', FALSE, NULL, 2),
('Our Solutions', '#', TRUE, NULL, 3),
('Contact Us', 'contact.html', FALSE, NULL, 6),
('BOOK DEMO', 'book-demo.html', FALSE, NULL, 7);

-- Example: Dropdown items for 'Our Solutions' (assuming 'Our Solutions' has id=3 from above insert)
-- You will need to get the actual ID of 'Our Solutions' after inserting it.
-- For example, if 'Our Solutions' gets ID 3, then:
INSERT INTO navbar_items (item_text, item_href, is_dropdown, parent_id, display_order) VALUES
('Matchmaking (Live Now)', 'matchmaking.html', FALSE, (SELECT id FROM navbar_items WHERE item_text='Our Solutions' AND is_dropdown=TRUE), 4),
('Upcoming Solutions (Launching Soon)', 'upcoming.html', FALSE, (SELECT id FROM navbar_items WHERE item_text='Our Solutions' AND is_dropdown=TRUE), 5);
