# AratBoi: what the app does, and which shop problem each part fixes

AratBoi (আরতবই, “the arat book”) is an Android app for a Bangladeshi rice wholesaler. The owner is usually 40 to 65. He already runs the shop on paper. He does not want a new way of doing business. He wants the evening to stop fighting him.

This note is for a friend who needs to understand the product. The phone itself stays in Bangla. Every Bangla label below is the words on the screen, followed by what they mean.

## The evening problem

One truck of rice is written four times:

1. How many sacks left the warehouse.
2. How much that buyer still owes the shop.
3. How much the shop still owes the mill, when the rice was not the shop’s.
4. How much paper cash should be in the drawer.

A clerk writes these in four notebooks, often at different moments, often with one line missed. At night the four books do not match. The owner stays late hunting the missing line. That hunt is the product. AratBoi does not “digitize the shop.” It types the sale once, and those four records move together, because they are the same entry. The books are stored on the phone, so a dead market wifi does not stop an invoice.

## The four books, in one glance

| Book on the phone | Meaning | What a plus or minus means |
|---|---|---|
| মাল খাতা (mal khata) | Warehouse book | Sacks of each rice still on hand |
| Buyer balance | Money still unpaid by a customer | Positive means they owe the shop |
| Mill balance | Money still unpaid to a rice mill | Stored as a negative number: the shop owes the mill |
| ক্যাশ বাক্স (cash box) | Paper notes in the drawer | Cash received minus cash paid. bKash, Nagad, and bank do not enter this box |

The bottom bar has four places:

- **হোম (Home)** — the four headline numbers.
- **খাতা (Khata, “the books”)** — warehouse, then cash.
- **নতুন চালান (Notun chalan, “new invoice”)** — the only button that writes all four books.
- **দেনা-পাওনা (Dena-paona, “debts and dues”)** — buyers and mills.

## Home: the four numbers he can point at

**The problem.** He will not explore an app. If the first screen is busy, he decides it is for his nephew and goes back to the red book. He also cannot answer “how many sacks, how much sold today, who owes me, what is in the drawer?” without opening four notebooks.

**What he does.** He looks at four equal cards. He taps one when he wants the book behind it.

| Card | What it shows | Tap opens |
|---|---|---|
| মোট মজুদ চাল | Sacks in the warehouse, and roughly how many maunds (1 maund = 40 kg) | Warehouse book |
| আজকের মোট বিক্রি | Sales dated today, in taka and sacks | A seven-day bar chart, and only after the tap |
| বাজারে মোট বাকি | Money buyers still owe the shop. Mills are not in this number | People list |
| ক্যাশ বাক্সে নগদ | Paper cash the drawer should hold | Cash book |

**What the app writes.** Nothing, until he saves an invoice or a payment. Home is a view of the four books.

**What it does not do.** The chart is not on the card. A graph on arrival feels like a gadget. A graph after he asks “how was this week?” is an answer. The stock card does not show the taka value of the rice. It shows sacks and maunds, which is how he counts. A short recent list of invoices sits under the cards. Opening one shows the receipt. Share sends that receipt as text through the phone’s share sheet (WhatsApp, and so on). There is no separate printer driver.

The shop name and the green **সক্রিয় (“active”)** pill open the shop name, owner, phone, and a device id. They do not lock the phone. More on that in the limits section.

## Own rice versus commission rice

**The problem.** A lot of the rice in the warehouse is not his. The mill sent it. He sells it, keeps a fee per sack, pays the unloaders, and owes the mill the rest. On paper that sum is easy to get wrong, and the mill does not forgive a wrong net. His own rice is a different deal: he already bought it, so the mill is not billed again.

**What he does.** Before typing, he picks one large card:

- **নিজস্ব চাল (nijosh chal)** — my own rice.
- **কমিশন চালান (commission chalan)** — the mill’s rice. I only earn a fee.

The header color changes with that choice, so the mode is obvious while he types.

**What the app writes, for commission rice.**

1. Sacks × selling price = the sale.
2. Fee per sack × sacks = the shop’s commission. That fee stays with the shop. It is not its own number on Home.
3. One labor total is subtracted. The unloaders (**খালাসি, khalasi**) are paid as a lump, not per sack. Truck fare has no box of its own. If he wants it in the mill’s bill, he adds it into this labor total.
4. The mill is owed sale − fee − labor. That net is never below zero, so a typing mistake cannot show the mill owing the shop.

Worked example, the one the trade already uses:

- 200 sacks × ৳3,000 = ৳6,00,000 sale.
- Commission ৳20 × 200 = ৳4,000 kept by the shop.
- Labor lump ৳13,600 (workers ৳1,600 plus truck fare ৳12,000, folded into the one box).
- Mill is owed ৳6,00,000 − ৳4,000 − ৳13,600 = ৳5,82,400.

The mill’s net is visible before he taps save, so he can argue with the number while someone is still standing there.

**What it does not do.** The buyer’s unpaid part does not reduce what the shop owes the mill. Those are two different debts. If the buyer pays ৳97,500 of a ৳1,42,500 sale by bKash and still owes ৳45,000, the mill is still owed the full net. Own-rice mode does not create a mill bill at all. Purchase mode turns the fee deal off, because buying rice is not a commission sale.

## Sale and purchase

**The problem.** A sale and a purchase move the warehouse in opposite directions, and they create debt on opposite people. Mixing them up is how a book starts lying.

**What he does.** On the invoice he first chooses:

- **বিক্রি চালান** — a sale. Sacks leave. The buyer owes whatever was not paid now.
- **ক্রয় চালান** — a purchase. Sacks arrive. The shop owes the mill whatever was not paid now.

Then he picks the rice (the price fills from that rice’s sell rate or buy rate), the sack count, the price per sack, and the person. A line under the sack count converts to maunds, because a 50 kg sack is 1.25 maunds and a 25 kg sack is 0.625. He enters how much is paid right now (**তাৎক্ষণিক পরিশোধ**) and whether that money was paper cash, bKash, Nagad, or bank. He can also type a truck plate. The plate is stored on the receipt only. It does not change money.

The save button says **চালান সংরক্ষণ ও ৪ খাতা আপডেট করুন**: save, and update the four books.

**What the app writes.**

- Own sale: sacks go down, the buyer’s unpaid amount goes up, paper cash goes up by the cash paid now.
- Commission sale: sacks go down, the buyer’s unpaid amount goes up, the mill is owed the net, paper cash goes up by cash paid now and down by the labor lump (workers are treated as paid in notes).
- Purchase: sacks go up, the shop owes the mill the unpaid part, paper cash goes down by the cash paid now.
- Paying more than the invoice becomes credit on that person. The extra is not thrown away.

**What it does not do.** A sale of more sacks than are on hand is refused, with the message that the warehouse does not have enough sacks. Commission rice uses the same sack pile as own rice. A commission truck has to be unloaded first (stock in) if those sacks are not already counted. The app will not invent sacks.

## People: who owes whom

**The problem.** A buyer walks in. The owner wants the due in one look, then a call or a WhatsApp chase. Searching a paper book in a loud market is slow, and it is embarrassing if he cannot find the page. He also meets new buyers and new mills in the middle of writing an invoice. Leaving the screen to “create a contact first” feels like work, so he will not do it.

**What he does.** The people screen has three filters:

- **সকল খাতা** — everyone.
- **দেনাদার (বাকি পাওনা)** — buyers, including a buyer who currently owes nothing.
- **পাওনাদার (মিলার)** — mills. The card shows the amount as “the mill will get this much,” even though the balance is stored as a negative number.

On a person he can:

- **কল** — dial the saved phone number.
- **WhatsApp তাগাদা** — open WhatsApp with a Bangla reminder of what they owe. This button shows only when they owe the shop.
- **বাকি আদায়** — the buyer is paying the shop.
- **টাকা দিন** — the shop is paying a mill.

He can add a person from this screen, or from the buyer and mill dropdowns on the invoice itself (**+ নতুন পার্টি**). The type is two large cards, not a small switch:

- **খরিদ্দার** — a buyer, someone the shop will collect from.
- **মিলার** — a mill, someone the shop will pay.

Search matches name, phone, or market address.

**What the app writes.** A collection or a payment moves that person’s balance. Only the paper-cash choice also moves the drawer. Paying more than the balance leaves a credit.

**What it does not do.** Home’s “money in the market” card adds up only buyer balances above zero. A mill the shop owes is not mixed into that card. A buyer who owes nothing still appears in the buyer filter, at zero, so he can be selected on the next invoice. WhatsApp is a reminder draft. It does not collect the money.

## Cash versus phone wallets

**The problem.** At closing he counts notes. If the book says one total and the drawer says another, he does not know whether a sale was written wrong or someone took cash. Phone wallets make this worse: bKash arrived, but the drawer did not, and a careless app would add it to both.

**What he does.** On the cash book (**ক্যাশ খাতা ও রোকার**) he filters all movements, money in (**নগদ জমা**), or money out (**নগদ খরচ / প্রদান**). He counts 1000, 500, 200, 100, 50, 20, and 10 taka notes (**ক্যাশ মিলান**) and saves that count (**ক্যাশ রোকার সংরক্ষণ করুন**). The count is still there after he leaves the app.

On an invoice or a payment he picks one method:

- **নগদ** — paper cash.
- **বিকাশ** — bKash.
- **নগদ পে** — the Nagad wallet. The name is easy to confuse with paper cash. It is not paper cash.
- **ব্যাংক** — bank transfer.

**What the app writes.** The cash-box number is paper cash received minus paper cash paid. A short drawer is shown as a negative number. It is not hidden as zero. The note count is a comparison. Saving it does not change the cash-box number.

**What it does not do.** bKash, Nagad, and bank are stored so he can see them later. They do not change the drawer. The “money in” filter lists every receipt, including bKash, because that chip means money in, not “paper notes.” The Home cash card is the paper-only number. Those two screens are supposed to differ when a wallet was used. The commission fee stays in the drawer. It is not pulled out into its own Home card.

## Rice list, warehouse adjustments, and empty sacks

**The problem.** Rice is not one product. Miniket, Nazirshail, BR-28, Kataribhog, Chinigura, and Swarna are different piles, different prices, and different sack sizes. A new variety shows up without warning. Sacks also move without a sale: a truck is unloaded, a shortage is found, a sample is taken out. Separately, buyers borrow empty jute sacks and forget to bring them back. That count is not money, and mixing it into the cash book makes the night check look wrong.

**What he does.**

- **নতুন চাল**, from the warehouse book or from the rice dropdown on an invoice (**+ নতুন চাল যোগ করুন**): name, 50 kg or 25 kg sack, opening sacks, sell price, buy price.
- **স্টক ইন/আউট**: change sacks without an invoice. In means a truck was unloaded. Out means a shortage or a sample. Unload a commission truck here before selling it, if those sacks are not already in the warehouse.
- Search filters the warehouse by rice name.
- **খালি বস্তা প্রদান**, on a sale: empty sacks handed to this buyer, added to that buyer’s empty-sack count. On the person form the field is **খালি বস্তা পাওনা (যদি থাকে)**, a starting count of bags they already owe back.

**What the app writes.** Each rice has its own sack pile. A sale or purchase of that rice moves only that pile. Empty sacks are a count of bags on the buyer.

**What it does not do.** Empty sacks are not taka. They do not change the cash box, the buyer’s money balance, or the mill’s bill. Stock in/out does not create a buyer due or a mill bill. It only fixes the sack count.

## Built so he does not put the phone down

**The problem.** Small text, a dark glowing screen, and two buttons that both mean “new invoice” make him feel the app is for someone younger. He will not hunt through settings to find the one action that matters.

**What he sees.** A warm cream screen, brown text, a muted green primary button, and a terracotta color where money is still owed. Cards are the same height. There is one green **নতুন চালান** button, and it gently pulses so the next action is obvious. Numbers are shown with Bangla digits. Own-versus-commission, and buyer-versus-mill, are large cards with a one-line explanation, not a buried toggle.

**What it does not do.** It does not teach him accounting. It uses the words he already uses: sack, due, mill, commission, paper cash. It does not translate the screen into English. English on the screen would mark it as not for him.

## A day that already ties out

A fresh install is not empty. It contains one finished story, saved through the same path as a real invoice, so a friend can see the four books agree before trusting the app with a live sale.

1. Two days ago, own rice: 30 sacks of Miniket at ৳3,450 to one buyer. Sale ৳1,03,500. Cash then ৳25,000. Unpaid ৳78,500.
2. That buyer later paid another ৳20,000 in cash. They now owe ৳58,500.
3. Yesterday, commission rice: 50 sacks of BR-28 at ৳2,850, from one mill. Sale ৳1,42,500. Fee ৳20 × 50 = ৳1,000. Labor lump ৳500, paid to workers in cash. Mill is owed ৳1,41,000. The buyer paid ৳97,500 by bKash and still owes ৳45,000.

Home then shows:

- 1,130 sacks on hand, about 1,281 maunds.
- Today’s sales ৳0, because both sample invoices are in the past.
- Buyers owe ৳1,03,500 (58,500 + 45,000).
- Paper cash ৳44,500 (25,000 + 20,000 − 500). The ৳97,500 bKash payment is not in this number.

That last line is the whole cash rule in one figure.

## What not to tell a shop owner

These are real limits. Saying otherwise will break the first evening he checks.

- **The credit limit does not block a sale.** The field **সর্বোচ্চ বাকি (০ = সীমা নেই)** means “maximum due. Zero means no limit.” If this invoice would push a buyer past that number, a warning appears. He can still save. Owners hate a tool that refuses a sale while a buyer is waiting.
- **The green “active” pill is a label.** The “30 days” line is a fixed sentence, not a live countdown. The phone does not lock. The device id is a display. The true promise is smaller: the invoice works with no internet, because the database is on this phone.
- **It does not replace his accountant.** It stops the four books drifting apart. It does not file tax, argue with the mill, or produce a year-end statement.
- **It does not sync to a second phone.** There is no cloud copy. The red book stays the master until he decides to leave it.
- **Installing this version wipes the on-phone database once.** That is so the sample story above can be rebuilt through the real save path. It is a one-time cost of this upgrade, not a daily behavior. After that, his new invoices stay on the phone.
- **Empty sacks are bags, not money. Wallet payments are not drawer cash. The labor box is one lump, not a per-sack rate.**

## What to try with him, if he is willing

One real sale he is about to write in the red book. Not the whole year. Choose own rice or commission rice first, enter sacks, price, buyer, and what was paid now, then save. Read the four Home numbers and that buyer’s balance against the line he was about to write. If they match, stop. Leave the old paper as the master for the rest of the day.

The close that respects his risk is one sentence: today’s one invoice, the old books stay, tonight he compares the two totals, and if they do not match he puts the phone away.
