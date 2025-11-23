package com.healthbridge.service.impl;


import org.springframework.stereotype.Service;

import com.healthbridge.entity.SymptomRecord;
import com.healthbridge.service.SeverityAssessmentService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SeverityAssessmentServiceImpl implements SeverityAssessmentService {

	@Override
	public void assess(SymptomRecord record, String aiResponse) {
		if (record == null) {
			log.error(" SymptomRecord is null. Cannot assess severity.");
			return;
		}

		int score = computeScore(record.getSymptoms(), aiResponse, record.getLanguage());
		String level = mapLevel(score, record.getLanguage());

		record.setSeverityScore(score);
		record.setSeverityLevel(level);
		record.setReferralNeeded(isReferralRequired(score, level)); // fixed logic

		log.info(" Assessed severity: {} | Score: {} | Language: {}", level, score, record.getLanguage());
	}

	/**
	 * Determines if referral to doctor/hospital is needed based on severity or
	 * keywords
	 */
	private boolean isReferralRequired(int score, String level) {
		if (score >= 8)
			return true; // any high or emergency condition
		return level.equalsIgnoreCase("HIGH") || level.equalsIgnoreCase("EMERGENCY") || level.equalsIgnoreCase("उच्च")
				|| level.equalsIgnoreCase("आपातकालीन") || level.equalsIgnoreCase("உயர்")
				|| level.equalsIgnoreCase("அவசரம்") || level.equalsIgnoreCase("అధిక")
				|| level.equalsIgnoreCase("అత్యవసర") || level.equalsIgnoreCase("ഉയർന്ന")
				|| level.equalsIgnoreCase("അത്യാഹിതം") || level.equalsIgnoreCase("ઉચ્ચ")
				|| level.equalsIgnoreCase("આપત્કાળીન") || level.equalsIgnoreCase("आपत्कालीन");
	}

	// Multilingual severity scoring (as before)
	private int computeScore(String symptoms, String aiResp, String language) {
		String text = ((symptoms == null ? "" : symptoms) + " " + (aiResp == null ? "" : aiResp)).toLowerCase();

		// English - Cardiovascular & Respiratory Emergencies
		if (containsAny(text,
				// Heart-related
				"chest pain", "pressure in chest", "chest pressure", "crushing chest pain", "tight chest",
				"heart attack", "cardiac arrest", "heart stopped", "irregular heartbeat", "palpitations",
				"rapid heartbeat", "slow heartbeat", "chest tightness",
				// Breathing emergencies
				"difficulty breathing", "can't breathe", "cannot breathe", "shortness of breath", "gasping for air",
				"choking", "suffocating", "respiratory distress", "blue lips", "bluish face", "cyanosis",
				"labored breathing", "wheezing severely",
				// Stroke symptoms
				"stroke", "face drooping", "arm weakness", "slurred speech", "sudden numbness", "paralysis",
				"can't move", "sudden confusion", "trouble speaking", "sudden severe headache", "worst headache",
				"thunderclap headache", "sudden vision loss", "double vision suddenly", "dizziness with numbness",
				// Bleeding & Trauma
				"severe bleeding", "uncontrolled bleeding", "bleeding won't stop", "hemorrhage", "heavy blood loss",
				"profuse bleeding", "spurting blood", "arterial bleeding", "internal bleeding", "bleeding internally",
				"vomiting blood", "blood in vomit", "coughing blood", "coughing up blood", "blood in urine",
				"rectal bleeding severe",
				// Consciousness & Neurological
				"unconscious", "passed out", "fainted", "loss of consciousness", "unresponsive", "not waking up",
				"seizure", "convulsions", "fitting", "severe head injury", "head trauma", "neck injury",
				"spinal injury",
				// Poisoning & Overdose
				"poisoned", "overdose", "swallowed poison", "chemical exposure", "drug overdose",
				// Severe Pain
				"excruciating pain", "unbearable pain", "severe abdominal pain", "sudden severe pain",
				"pain radiating to jaw", "pain in left arm", "pain radiating to back",
				// Allergic Reactions
				"anaphylaxis", "severe allergic reaction", "throat swelling", "tongue swelling",
				"difficulty swallowing", "allergic shock",
				// Other Critical
				"suspected appendicitis", "burst appendix", "ruptured organ", "internal injury", "severe burns",
				"electrical burn", "smoke inhalation", "near drowning", "major trauma", "compound fracture",
				"bone protruding", "suicide attempt", "thoughts of suicide", "self harm", "harming myself",
				"want to die"))
			return 10;

		// Hindi - Critical
		if (containsAny(text, "छाती में दर्द", "छाती में दबाव", "सांस लेने में तकलीफ", "सांस नहीं ले सकता",
				"दम घुट रहा है", "बेहोशी", "बेहोश", "चक्कर आना", "भारी खून बहना", "खून बह रहा है", "दिल का दौरा",
				"दिल की धड़कन तेज", "धड़कन अनियमित", "स्ट्रोक", "लकवा", "बोलने में दिक्कत", "चेहरा झुका हुआ",
				"हाथ कमजोर", "अचानक सुन्न", "गंभीर दर्द", "असहनीय दर्द", "जहर", "आत्महत्या", "गंभीर जलन",
				"खून की उल्टी", "खून खांसना", "बेहोश हो गया", "दौरा पड़ना", "मिर्गी", "गंभीर सिर की चोट",
				"गर्दन में चोट"))
			return 10;

		// Gujarati - Critical
		if (containsAny(text, "છાતીમાં દુખાવો", "છાતીમાં દબાણ", "શ્વાસ લેવામાં તકલીફ", "શ્વાસ લઈ શકતા નથી", "ગૂંગળામણ",
				"બેભાન", "ચક્કર આવવા", "રક્તસ્ત્રાવ", "ખૂબ લોહી વહે છે", "હાર્ટ એટેક", "ધબકારા અનિયમિત", "સ્ટ્રોક",
				"લકવો", "બોલવામાં મુશ્કેલી", "ચહેરો નમી ગયો", "હાથ નબળા", "અચાનક સુન્ન", "ગંભીર દુખાવો", "અસહ્ય વેદના",
				"ઝેર", "આત્મહત્યા", "ગંભીર દાઝવું", "લોહીની ઉલટી", "લોહી ખાંસી", "બેભાન થઈ ગયા", "હુમલો", "મરકી",
				"ગંભીર માથાની ઇજા", "ગરદનમાં ઇજા"))
			return 10;

		// Marathi - Critical
		if (containsAny(text, "छातीत दुखणे", "छातीत दबाव", "श्वास घेण्यात त्रास", "श्वास घेऊ शकत नाही", "गुदमरणे",
				"बेशुद्ध", "चक्कर येणे", "रक्तस्त्राव", "खूप रक्त वाहते आहे", "हृदयविकाराचा झटका", "धडधडणे अनियमित",
				"पक्षाघात", "अर्धांगवायू", "बोलण्यात अडचण", "चेहरा लटकलेला", "हात कमकुवत", "अचानक सुन्न", "तीव्र वेदना",
				"असह्य वेदना", "विष", "आत्महत्या", "गंभीर भाजणे", "रक्ताची उलटी", "रक्त खोकला", "बेशुद्ध झाले",
				"फिट येणे", "अपस्मार", "गंभीर डोक्याची दुखापत", "मानेची दुखापत"))
			return 10;

		// Tamil - Critical
		if (containsAny(text, "மார்வலி", "மார்பு அழுத்தம்", "மூச்சுத்திணறல்", "மூச்சு விட முடியவில்லை",
				"மூச்சுத் திணறல்", "மயக்கம்", "தலைசுற்றல்", "காயம்", "அதிக இரத்தப்போக்கு", "இதய நோய்", "இதயத் துடிப்பு",
				"பக்கவாதம்", "முககூம்பல்", "பேச்சு சிரமம்", "முகம் தொங்குதல்", "கை பலவீனம்", "திடீர் மரத்துப்போதல்",
				"கடுமையான வலி", "தாங்க முடியாத வலி", "விஷம்", "தற்கொலை", "கடுமையான தீக்காயம்", "இரத்த வாந்தி",
				"இரத்த இருமல்", "மயக்கமடைந்தது", "வலிப்பு", "கால்-கை வலிப்பு", "கடுமையான தலை காயம்", "கழுத்து காயம்"))
			return 10;

		// Telugu - Critical
		if (containsAny(text, "ఛాతి నొప్పి", "ఛాతి ఒత్తిడి", "శ్వాస తీసుకోవడంలో ఇబ్బంది", "శ్వాస తీసుకోలేకపోవడం",
				"ఉక్కిరిబిక్కిరి", "అస్వస్థత", "తలతిరగడం", "గాయం", "అధిక రక్తస్రావం", "గుండెపోటు", "హృదయ స్పందన",
				"స్ట్రోక్", "పక్షవాతం", "మాట్లాడటంలో ఇబ్బంది", "ముఖం వంగడం", "చేయి బలహీనత", "అకస్మాత్తుగా తిమ్మిరి",
				"తీవ్ర నొప్పి", "భరించలేని నొప్పి", "విషం", "ఆత్మహత్య", "తీవ్రమైన కాలిపోవడం", "రక్తపు వాంతి",
				"రక్తపు దగ్గు", "స్పృహ కోల్పోవడం", "మూర్ఛ", "మూర్ఛ వ్యాధి", "తీవ్రమైన తల గాయం", "మెడ గాయం"))
			return 10;

		// Malayalam - Critical
		if (containsAny(text, "നെഞ്ചുവേദന", "നെഞ്ച് സമ്മർദ്ദം", "ശ്വാസംമുട്ടൽ", "ശ്വസിക്കാൻ കഴിയുന്നില്ല",
				"ശ്വാസം മുട്ടൽ", "ബോധരഹിതം", "തലകറക്കം", "മുറിവ്", "അമിത രക്തസ്രാവം", "ഹൃദയാഘാതം", "ഹൃദയമിടിപ്പ്",
				"പക്ഷാഘാതം", "തളർവാതം", "സംസാരിക്കാൻ ബുദ്ധിമുട്ട്", "മുഖം തൂങ്ങൽ", "കൈ ബലഹീനത", "പെട്ടെന്ന് മരവിപ്പ്",
				"തീവ്ര വേദന", "അസഹനീയമായ വേദന", "വിഷം", "ആത്മഹത്യ", "ഗുരുതരമായ പൊള്ളൽ", "രക്തം ഛർദ്ദി", "രക്തം ചുമ",
				"ബോധം നഷ്ടം", "പിടിപത്ത്", "അപസ്മാരം", "ഗുരുതരമായ തലയ്ക്ക് പരിക്ക്", "കഴുത്തിന് പരിക്ക്"))
			return 10;

		// HIGH SEVERITY - Score 8-9 (Serious conditions requiring urgent care)
		// English - Serious Symptoms
		if (containsAny(text,
				// High Fever & Infections
				"high fever", "fever over 103", "fever above 39", "persistent fever", "fever with rash",
				"fever with stiff neck", "fever with confusion", "sepsis", "septic shock", "severe infection",
				"infected wound", "pus", "abscess", "cellulitis spreading",
				// Severe Pain
				"persistent vomiting", "severe vomiting", "can't keep fluids down", "dehydration", "severe pain",
				"intense pain", "pain won't go away", "worsening pain", "severe headache", "migraine",
				"cluster headache", "sudden headache", "severe back pain", "kidney pain", "flank pain",
				"passing kidney stone", "severe abdominal pain", "stomach pain severe", "appendix pain",
				// Mental Health Emergencies
				"suicidal thoughts", "want to harm myself", "depression severe", "panic attack severe", "psychosis",
				"hallucinations", "delusions", "bipolar crisis", "manic episode",
				// Pregnancy Related
				"pregnancy bleeding", "severe pregnancy pain", "decreased fetal movement", "pregnancy complications",
				"preeclampsia", "eclampsia",
				// Other Serious
				"blood in stool", "black stool", "tarry stool", "blood in urine", "severe allergic reaction",
				"swelling rapidly", "hives all over", "severe asthma attack", "asthma not responding", "peak flow low",
				"diabetic emergency", "blood sugar very high", "blood sugar very low", "hypoglycemia", "hyperglycemia",
				"ketoacidosis", "severe diarrhea", "bloody diarrhea", "persistent diarrhea", "cholera symptoms",
				"vision loss", "sudden vision change", "eye injury", "eye pain severe", "testicular pain",
				"testicular torsion", "groin pain severe"))
			return 8;

		// Hindi - Serious
		if (containsAny(text, "तेज बुखार", "बुखार 103 से ऊपर", "लगातार बुखार", "बुखार और दाने", "बुखार और गर्दन अकड़ना",
				"संक्रमण", "सेप्सिस", "गंभीर संक्रमण", "लगातार उल्टी", "तेज उल्टी", "पानी नहीं रुक रहा", "डिहाइड्रेशन",
				"तेज दर्द", "गंभीर दर्द", "दर्द बढ़ रहा है", "पेट में तेज दर्द", "माइग्रेन", "गंभीर सिरदर्द",
				"पीठ में तेज दर्द", "गुर्दे की पथरी", "खून", "खून दिख रहा है", "काला मल", "पेशाब में खून",
				"आत्मघाती विचार", "खुद को नुकसान", "गंभीर अवसाद", "भ्रम", "मतिभ्रम", "गर्भावस्था में खून",
				"गर्भावस्था जटिलताएं", "शुगर बहुत अधिक", "शुगर बहुत कम", "खूनी दस्त", "गंभीर दस्त", "आंख में दर्द",
				"दृष्टि खो रही है"))
			return 8;

		// Gujarati - Serious
		if (containsAny(text, "ઉંચો તાવ", "તાવ 103 થી ઉપર", "સતત તાવ", "તાવ અને ફોલ્લીઓ", "તાવ અને ગરદન જકડાવું", "ચેપ",
				"સેપ્સિસ", "ગંભીર ચેપ", "સતત ઉલટી", "તીવ્ર ઉલટી", "પાણી રોકાતું નથી", "ડિહાઇડ્રેશન", "તીવ્ર દુખાવો",
				"ગંભીર દુખાવો", "દુખાવો વધી રહ્યો છે", "પેટમાં તીવ્ર દુખાવો", "માઇગ્રેન", "ગંભીર માથાનો દુખાવો",
				"પીઠમાં તીવ્ર દુખાવો", "કિડનીની પથરી", "લોહી", "લોહી દેખાય છે", "કાળો મળ", "પેશાબમાં લોહી",
				"આત્મહત્યાના વિચારો", "પોતાને નુકસાન", "ગંભીર ડિપ્રેશન", "ભ્રમણા", "મનોવિકૃતિ",
				"ગર્ભાવસ્થામાં રક્તસ્રાવ", "ગર્ભાવસ્થા ગૂંચવણો", "સુગર ખૂબ વધારે", "સુગર ખૂબ ઓછી", "લોહીયુક્ત ઝાડા",
				"ગંભીર ઝાડા", "આંખમાં દુખાવો", "દૃષ્ટિ ગુમાવવી"))
			return 8;

		// Marathi - Serious
		if (containsAny(text, "जास्त ताप", "ताप 103 वर", "सतत ताप", "ताप आणि पुरळ", "ताप आणि मान ताठ", "संसर्ग",
				"सेप्सिस", "गंभीर संसर्ग", "सतत उलटी", "तीव्र उलटी", "पाणी राहत नाही", "निर्जलीकरण", "तीव्र वेदना",
				"गंभीर वेदना", "वेदना वाढत आहे", "पोटात तीव्र वेदना", "मायग्रेन", "गंभीर डोकेदुखी", "पाठीत तीव्र वेदना",
				"मूत्रपिंडाचा दगड", "रक्त", "रक्त दिसत आहे", "काळा विष्ठा", "मूत्रात रक्त", "आत्महत्येचे विचार",
				"स्वतःला इजा", "गंभीर नैराश्य", "भ्रम", "मनोविकार", "गर्भधारणेत रक्तस्त्राव", "गर्भधारणा गुंतागुंत",
				"साखर खूप जास्त", "साखर खूप कमी", "रक्ताचा जुलाब", "गंभीर जुलाब", "डोळ्यात वेदना", "दृष्टी जात आहे"))
			return 8;

		// Tamil - Serious
		if (containsAny(text, "அதிக காய்ச்சல்", "காய்ச்சல் 103 மேல்", "தொடர் காய்ச்சல்", "காய்ச்சல் மற்றும் தடிப்பு",
				"காய்ச்சல் மற்றும் கழுத்து விறைப்பு", "தொற்று", "செப்சிஸ்", "கடுமையான தொற்று",
				"மீண்டும் மீண்டும் வாந்தி", "தீவிர வாந்தி", "நீர் தங்காது", "நீரிழப்பு", "தீவிர வலி", "கடுமையான வலி",
				"வலி அதிகரிக்கிறது", "வயிற்றில் தீவிர வலி", "ஒற்றைத் தலைவலி", "கடுமையான தலைவலி", "முதுகில் தீவிர வலி",
				"சிறுநீரக கல்", "இரத்தம்", "இரத்தம் தெரிகிறது", "கருப்பு மலம்", "சிறுநீரில் இரத்தம்",
				"தற்கொலை எண்ணங்கள்", "சுயவிருத்தம்", "கடுமையான மனச்சோர்வு", "மாயத்தோற்றம்", "மனநோய்",
				"கர்ப்பத்தில் இரத்தப்போக்கு", "கர்ப்ப சிக்கல்கள்", "சர்க்கரை மிக அதிகம்", "சர்க்கரை மிகக் குறைவு",
				"இரத்த வயிற்றுப்போக்கு", "கடுமையான வயிற்றுப்போக்கு", "கண் வலி", "பார்வை இழப்பு"))
			return 8;

		// Telugu - Serious
		if (containsAny(text, "అధిక జ్వరం", "జ్వరం 103 పైన", "నిరంతర జ్వరం", "జ్వరం మరియు దద్దుర్లు",
				"జ్వరం మరియు మెడ దృఢత్వం", "సంక్రమణ", "సెప్సిస్", "తీవ్రమైన సంక్రమణ", "తరచూ వాంతులు", "తీవ్ర వాంతులు",
				"నీరు ఆగడం లేదు", "డీహైడ్రేషన్", "తీవ్ర నొప్పి", "తీవ్రమైన నొప్పి", "నొప్పి పెరుగుతోంది",
				"కడుపులో తీవ్ర నొప్పి", "మైగ్రేన్", "తీవ్రమైన తలనొప్పి", "వెన్నులో తీవ్ర నొప్పి", "మూత్రపిండాల రాయి",
				"రక్తం", "రక్తం కనిపిస్తోంది", "నల్లని మలం", "మూత్రంలో రక్తం", "ఆత్మహత్య ఆలోచనలు", "స్వయంహాని",
				"తీవ్రమైన మాంద్యం", "భ్రమలు", "మానసిక రుగ్మత", "గర్భధారణలో రక్తస్రావం", "గర్భధారణ సమస్యలు",
				"షుగర్ చాలా ఎక్కువ", "షుగర్ చాలా తక్కువ", "రక్తపు డయ్యరియా", "తీవ్రమైన డయ్యరియా", "కంటి నొప్పి",
				"దృష్టి కోల్పోవడం"))
			return 8;

		// Malayalam - Serious
		if (containsAny(text, "ഉയർന്ന ജ്വരം", "ജ്വരം 103 ന് മേൽ", "തുടർച്ചയായ ജ്വരം", "ജ്വരവും തിണർപ്പും",
				"ജ്വരവും കഴുത്ത് ദൃഢതയും", "അണുബാധ", "സെപ്സിസ്", "ഗുരുതരമായ അണുബാധ", "തുടർച്ചയായ ഛർദ്ദി",
				"തീവ്ര ഛർദ്ദി", "വെള്ളം നിൽക്കുന്നില്ല", "നിർജ്ജലീകരണം", "തീവ്ര വേദന", "ഗുരുതരമായ വേദന",
				"വേദന വർദ്ധിക്കുന്നു", "വയറ്റിൽ തീവ്ര വേദന", "മൈഗ്രേൻ", "ഗുരുതരമായ തലവേദന", "പുറകിൽ തീവ്ര വേദന",
				"വൃക്കയിലെ കല്ല്", "രക്തം", "രക്തം കാണുന്നു", "കറുത്ത മലം", "മൂത്രത്തിൽ രക്തം", "ആത്മഹത്യാ ചിന്തകൾ",
				"സ്വയം ദ്രോഹം", "ഗുരുതരമായ വിഷാദം", "മിഥ്യാധാരണകൾ", "മനഃരോഗം", "ഗർഭാവസ്ഥയിൽ രക്തസ്രാവം",
				"ഗർഭാവസ്ഥ സങ്കീർണതകൾ", "പഞ്ചസാര വളരെ കൂടുതൽ", "പഞ്ചസാര വളരെ കുറവ്", "രക്തമുള്ള വയറിളക്കം",
				"ഗുരുതരമായ വയറിളക്കം", "കണ്ണ് വേദന", "കാഴ്ച നഷ്ടം"))
			return 8;

		// MODERATE SEVERITY - Score 6-7 (Requires medical attention but not emergency)
		// English - Moderate Symptoms
		if (containsAny(text,
				// Common illnesses
				"fever", "temperature", "running fever", "feverish", "chills", "cough", "persistent cough", "coughing",
				"dry cough", "wet cough", "phlegm", "cold", "flu", "influenza", "common cold", "viral infection",
				"sore throat", "throat pain", "strep throat", "tonsillitis", "pharyngitis", "headache", "head pain",
				"tension headache", "sinus headache",
				// Gastrointestinal
				"nausea", "feeling sick", "queasy", "upset stomach", "vomiting", "throwing up", "vomit", "puking",
				"diarrhea", "loose stools", "stomach bug", "gastroenteritis", "constipation", "bloating", "gas",
				"indigestion", "acid reflux", "heartburn", "stomach ache", "stomach pain", "abdominal discomfort",
				"cramps", "stomach cramps",
				// Pain & Discomfort
				"body ache", "muscle pain", "joint pain", "back pain", "neck pain", "toothache", "dental pain",
				"tooth pain", "jaw pain", "earache", "ear pain", "ear infection", "sprain", "strain", "twisted ankle",
				"pulled muscle",
				// Respiratory
				"congestion", "stuffy nose", "runny nose", "sneezing", "sinus pressure", "wheezing", "chest congestion",
				"bronchitis",
				// Skin conditions
				"rash", "skin rash", "itching", "itchy skin", "hives", "eczema", "psoriasis", "skin infection",
				"wound infection", "cut infected",
				// Urinary
				"urinary tract infection", "uti", "painful urination", "burning urination", "frequent urination",
				"bladder infection",
				// General symptoms
				"fatigue", "tired", "exhausted", "weakness", "dizzy", "lightheaded", "loss of appetite", "not eating",
				"weight loss sudden", "swelling", "swollen", "inflammation", "bruising", "insomnia", "can't sleep",
				"sleep problems"))
			return 6;

		// Hindi - Moderate
		if (containsAny(text, "बुखार", "तापमान", "ठंड लगना", "सर्दी", "जुकाम", "फ्लू", "खांसी", "लगातार खांसी",
				"सूखी खांसी", "बलगम", "गले में दर्द", "गला खराब", "टॉन्सिल", "सिरदर्द", "सिर में दर्द",
				"माइग्रेन हल्का", "उल्टी", "जी मिचलाना", "मतली", "पेट खराब", "दस्त", "पतले दस्त", "पेट दर्द", "कब्ज",
				"गैस", "एसिडिटी", "शरीर दर्द", "मांसपेशियों में दर्द", "जोड़ों में दर्द", "पीठ दर्द", "दांत दर्द",
				"कान दर्द", "कान में संक्रमण", "नाक बंद", "नाक बहना", "छींक आना", "साइनस", "घरघराहट", "ब्रोंकाइटिस",
				"दाने", "खुजली", "त्वचा में खुजली", "एक्जिमा", "मूत्र संक्रमण", "पेशाब में जलन", "बार-बार पेशाब",
				"थकान", "कमजोरी", "चक्कर", "भूख न लगना", "सूजन", "नींद नहीं आना"))
			return 6;

		// Gujarati - Moderate
		if (containsAny(text, "તાવ", "તાપમાન", "ઠંડી લાગવી", "શરદી", "ફ્લૂ", "ખાંસી", "સતત ખાંસી", "સૂકી ખાંસી", "કફ",
				"ગળામાં દુખાવો", "ગળું ખરાબ", "ટોન્સિલ", "માથાનો દુખાવો", "માથામાં દુખાવો", "ઉલટી", "ઉબકા", "પેટ ખરાબ",
				"ઝાડા", "પાતળા ઝાડા", "પેટમાં દુખાવો", "કબજિયાત", "ગેસ", "એસિડિટી", "શરીરમાં દુખાવો",
				"સ્નાયુમાં દુખાવો", "સાંધામાં દુખાવો", "પીઠનો દુખાવો", "દાંતનો દુખાવો", "કાનનો દુખાવો", "કાનમાં ચેપ",
				"નાક બંધ", "નાક વહેવું", "છીંક આવવી", "સાઇનસ", "શ્વાસ ફૂલવો", "બ્રોન્કાઇટિસ", "ફોલ્લીઓ", "ખંજવાળ",
				"ત્વચામાં ખંજવાળ", "એક્ઝીમા", "મૂત્ર ચેપ", "પેશાબમાં બળતરા", "વારંવાર પેશાબ", "થકાન", "નબળાઇ", "ચક્કર",
				"ભૂખ ન લાગવી", "સોજો", "ઊંઘ ન આવવી"))
			return 6;

		// Marathi - Moderate
		if (containsAny(text, "ताप", "तापमान", "थंडी लागणे", "सर्दी", "फ्लू", "खोकला", "सतत खोकला", "कोरडा खोकला", "कफ",
				"घशात दुखणे", "घसा खवखवणे", "टॉन्सिल", "डोकेदुखी", "डोक्यात दुखणे", "उलटी", "मळमळ", "पोट खराब", "जुलाब",
				"पातळ जुलाब", "पोटात दुखणे", "बद्धकोष्ठता", "गॅस", "आम्लपित", "शरीरात दुखणे", "स्नायूंमध्ये दुखणे",
				"सांध्यात दुखणे", "पाठ दुखणे", "दात दुखणे", "कान दुखणे", "कानात संसर्ग", "नाक बंद", "नाक वाहणे",
				"शिंका येणे", "सायनस", "घरघर", "ब्राँकायटिस", "पुरळ", "खाज", "त्वचेत खाज", "एक्झिमा", "मूत्र संसर्ग",
				"लघवीत जळजळ", "वारंवार लघवी", "थकवा", "अशक्तपणा", "चक्कर", "भूक नसणे", "सूज", "झोप येत नाही"))
			return 6;

		// Tamil - Moderate
		if (containsAny(text, "காய்ச்சல்", "வெப்பநிலை", "குளிர்", "சளி", "காய்ச்சல் ஃப்ளூ", "இருமல்", "தொடர் இருமல்",
				"உலர் இருமல்", "சளி", "தொண்டை வலி", "தொண்டை கரகரப்பு", "டான்சில்", "தலைவலி", "தலையில் வலி", "வாந்தி",
				"குமட்டல்", "வயிறு கெட்டது", "வயிற்றுப்போக்கு", "மலம் தளர்வு", "வயிற்று வலி", "மலச்சிக்கல்", "வாயு",
				"அமிலத்தன்மை", "உடல் வலி", "தசை வலி", "மூட்டு வலி", "முதுகு வலி", "பல் வலி", "காது வலி", "காது தொற்று",
				"மூக்கடைப்பு", "மூக்கு ஒழுகுதல்", "தும்மல்", "சைனஸ்", "மூச்சுத்திணறல்", "மூச்சுக்குழாய் அழற்சி",
				"தடிப்பு", "அரிப்பு", "தோல் அரிப்பு", "அரிக்கும் தோல்வியாதி", "சிறுநீர் தொற்று", "சிறுநீர் எரிச்சல்",
				"அடிக்கடி சிறுநீர்", "சோர்வு", "பலவீனம்", "தலைசுற்றல்", "பசியின்மை", "வீக்கம்", "தூக்கம் வராது"))
			return 6;

		// Telugu - Moderate
		if (containsAny(text, "జ్వరం", "ఉష్ణోగ్రత", "చలి", "జలుబు", "ఫ్లూ", "దగ్గు", "నిరంతర దగ్గు", "పొడి దగ్గు",
				"కఫం", "గొంతు నొప్పి", "గొంతు చిక్కుట", "టాన్సిల్స్", "తలనొప్పి", "తలలో నొప్పి", "వాంతులు", "వికారం",
				"కడుపు చెడిపోవడం", "డయ్యరియా", "వదులుగా మలం", "కడుపు నొప్పి", "మలబద్ధకం", "గ్యాస్", "ఆమ్లత్వం",
				"శరీర నొప్పి", "కండరాల నొప్పి", "కీళ్ళ నొప్పి", "వెన్ను నొప్పి", "పంటి నొప్పి", "చెవి నొప్పి",
				"చెవి సంక్రమణ", "ముక్కు మూసుకుపోవడం", "ముక్కు కారడం", "తుమ్ములు", "సైనస్", "గురక", "బ్రాంకైటిస్",
				"దద్దుర్లు", "దురద", "చర్మం దురద", "తామర", "మూత్ర సంక్రమణ", "మూత్రవిసర్జనలో మంట", "తరచుగా మూత్రవిసర్జన",
				"అలసట", "బలహీనత", "తలతిరగడం", "ఆకలి లేకపోవడం", "వాపు", "నిద్ర రావడం లేదు"))
			return 6;

		// Malayalam - Moderate
		if (containsAny(text, "ജ്വരം", "താപനില", "തണുപ്പ്", "ജലദോഷം", "ഫ്ലൂ", "ചുമ", "തുടർച്ചയായ ചുമ", "ഉണങ്ങിയ ചുമ",
				"കഫം", "തൊണ്ടവേദന", "തൊണ്ട വരൾച്ച", "ടോൺസിൽ", "തലവേദന", "തലയിൽ വേദന", "ഛർദ്ദി", "ഓക്കാനം",
				"വയറ് കേടായി", "അതിസാരം", "അയഞ്ഞ മലം", "വയറുവേദന", "മലബന്ധം", "വാതം", "അമ്ലത്വം", "ശരീര വേദന",
				"പേശി വേദന", "സന്ധി വേദന", "പുറം വേദന", "പല്ലുവേദന", "ചെവി വേദന", "ചെവി അണുബാധ", "മൂക്ക് പൂട്ടൽ",
				"മൂക്കൊലിപ്പ്", "തുമ്മൽ", "സൈനസ്", "ശ്വാസംമുട്ടൽ", "ബ്രോങ്കൈറ്റിസ്", "തിണർപ്പ്", "ചൊറിച്ചിൽ",
				"ചർമ്മ ചൊറിച്ചിൽ", "എക്സിമ", "മൂത്ര അണുബാധ", "മൂത്രമൊഴിക്കൽ എരിവ്", "പതിവായി മൂത്രമൊഴിക്കൽ", "ക്ഷീണം",
				"ബലഹീനത", "തലചുറ്റൽ", "വിശപ്പില്ലായ്മ", "വീക്കം", "ഉറക്കമില്ലായ്മ"))
			return 6;

		// LOW SEVERITY - Score 3-5 (Minor issues, self-care possible)
		return 3;
	}

	private boolean containsAny(String text, String... keys) {
		for (String k : keys)
			if (text.contains(k))
				return true;
		return false;
	}

	private String mapLevel(int score, String language) {
		String lang = (language == null) ? "en" : language.toLowerCase();
		switch (lang) {
		case "hi":
			if (score >= 9)
				return "आपातकालीन";
			if (score >= 7)
				return "उच्च";
			if (score >= 4)
				return "मध्यम";
			return "निम्न";
		case "gu":
			if (score >= 9)
				return "આપત્કાળીન";
			if (score >= 7)
				return "ઉચ્ચ";
			if (score >= 4)
				return "મધ્યમ";
			return "નીચું";
		case "mr":
			if (score >= 9)
				return "आपत्कालीन";
			if (score >= 7)
				return "उच्च";
			if (score >= 4)
				return "मध्यम";
			return "कमी";
		case "ta":
			if (score >= 9)
				return "அவசரம்";
			if (score >= 7)
				return "உயர்";
			if (score >= 4)
				return "நடுத்தர";
			return "குறைந்த";
		case "te":
			if (score >= 9)
				return "అత్యవసర";
			if (score >= 7)
				return "అధిక";
			if (score >= 4)
				return "మధ్యస్థ";
			return "తక్కువ";
		case "ml":
			if (score >= 9)
				return "അത്യാഹിതം";
			if (score >= 7)
				return "ഉയർന്ന";
			if (score >= 4)
				return "ഇടത്തരം";
			return "താഴ്ന്ന";
		default:
			if (score >= 9)
				return "EMERGENCY";
			if (score >= 7)
				return "HIGH";
			if (score >= 4)
				return "MEDIUM";
			return "LOW";
		}
	}
}