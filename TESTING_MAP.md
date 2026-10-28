# AratBoi testing map

The screens stay in Bangla. This file is the plain-English key for every tab and button, plus the numbers a fresh install should show.

Updating to this version wipes the on-phone database once. The demo invoices are then written through the same save path as a real invoice, so Home matches the sample story below.

## How one invoice hits the books

One saved invoice updates four books:

- Warehouse: sacks of rice on hand
- Customer book: money buyers still owe you
- Miller book: money you still owe the rice mill
- Cash box: paper notes only. bKash, Nagad, and bank do not go in this box

Two kinds of sale:

- **Own rice** (`নিজস্ব চাল`). You bought it. Profit is sell price minus what you paid the mill. The invoice does not create a mill bill.
- **Commission rice** (`কমিশন চালান` / `চালানী মাল`). The mill still owns the rice. You sell it, keep a fee per sack, subtract the worker bill, and the mill is owed the rest. The buyer's unpaid amount is a separate debt. It does not reduce what you owe the mill.

Worked example from the guideline. Type sack count `200`, price per sack `3000`, commission per sack `20`, and labor **total** `13600` (worker bill 1,600 plus truck fare 12,000, because there is no truck-fare box). Result: sale 600,000, your fee 4,000, mill is owed 582,400. You also need at least 200 sacks of that rice already in the warehouse, or the save is refused.

What one save writes:

- Own sale: sacks go down, the buyer's unpaid amount goes up, cash goes up by the cash paid now.
- Commission sale: sacks go down, the buyer's unpaid amount goes up, the mill is owed `sale − fee − labor total`, cash goes up by cash paid now and down by the labor total (workers are treated as paid in notes). Your fee stays in the drawer. It is not its own Home number.
- Purchase: sacks go up, you owe the mill the unpaid amount, cash goes down by the cash you paid now.

Paying more than the invoice total becomes credit on that person. The extra is not dropped.

## Words

- চাল = rice. বস্তা = sack. মণ = 40 kg. চালান = invoice. খাতা = book. গোডাউন = warehouse.
- মিলার = rice mill. খরিদ্দার / ক্রেতা = buyer.
- দেনাদার = a buyer who owes you. পাওনাদার = a mill you owe.
- বাকি = unpaid money. খালাসি / কুলি = unloaders. রোকার = end-of-day note count.
- নগদ = paper cash. নগদ পে = the Nagad wallet (not cash). বিকাশ = bKash.
- খালি বস্তা = empty sacks you lent a buyer. তাগাদা = a payment reminder.

## Bottom bar

- **হোম** — Home. Four numbers: sacks in the warehouse, today's sales, money buyers owe you, paper cash in the drawer.
- **খাতা** — Books. Two chips inside: warehouse, then cash.
- **নতুন চালান** — New invoice. This is the button that updates all four books.
- **দেনা-পাওনা** — People. Buyers and mills, with call, WhatsApp reminder, and take or pay money.

## Home

- **মোট মজুদ চাল** — sacks on hand, roughly how many 40 kg maunds, and those sacks valued at the selling price. Tap opens the warehouse book.
- **আজকের মোট বিক্রি** — sales dated today only. The sample invoices are dated yesterday and the day before, so this card is 0 on a fresh install. That is expected.
- **বাজারে মোট বাকি** — sum of money buyers owe you. Tap opens the people list. It does not include money you owe mills.
- **ক্যাশ বাক্সে নগদ** — paper cash only. Tap opens the cash book.

Shop name on the left and the green **সক্রিয়** pill on the right open shop name, owner, phone, and a device id. They do not lock the app. The "30 days" line is a fixed label, not a live countdown.

The **নতুন চালান** button is the same as the bottom-bar invoice tab. A recent invoice opens the receipt. Share sends the receipt text. Print is the system share sheet.

## খাতা — warehouse chip `মাল খাতা (চাল স্টক)`

- **নতুন চাল** — add a rice name, 50 kg or 25 kg sack, opening sacks, sell price, and buy price.
- **স্টক ইন/আউট** — change sacks without an invoice. Stock in means a truck was unloaded. Out means a shortage or a sample taken out. Unload a commission truck here before you sell it, if those sacks are not already in the warehouse. Commission sales use the same sack pile as own rice.
- The search box filters by rice name.
- A sale of more sacks than are on hand is refused.

## খাতা — cash chip `ক্যাশ খাতা ও রোকার`

- **ক্যাশ মিলান** — count 1000, 500, 200, 100, 50, 20, and 10 taka notes and compare the total with the cash-box number.
- **ক্যাশ রোকার সংরক্ষণ করুন** — remembers that note count on the phone. It does not change the cash-box number. The count is still there after you leave the app.
- Filters: all movements, cash in (**নগদ জমা**), cash out (**নগদ খরচ / প্রদান**).
- A short drawer shows as a negative number. It is not hidden as zero.

## New invoice `নতুন চালান`

- **বিক্রি চালান** — a sale. **ক্রয় চালান** — you buy rice into the warehouse and owe the mill the unpaid part.
- **নিজস্ব চাল** — own rice. **কমিশন চালান** — the mill's rice, fee deal. Purchase mode turns the fee deal off.
- **চাল নির্বাচন করুন** — pick the rice. The price fills from that rice's sell or buy rate.
- **বস্তার সংখ্যা** — how many sacks. **দর (প্রতি বস্তা)** — price per sack. The line under them converts to maunds (sack kg ÷ 40).
- **ক্রেতা নির্বাচন করুন** — the buyer, on a sale. A yellow line appears if this invoice would pass that buyer's credit limit. You can still save.
- **মিলার নির্বাচন করুন** — required for a commission sale and for a purchase.
- **কমিশন (প্রতি বস্তা)** — your fee, taka per sack. The app multiplies it by the sack count.
- **লেবার / খালাসি খরচ** — one total taka amount, not per sack. The sample default `500` means 500 taka for the whole invoice. There is no truck-fare box. Add truck fare into this total when you want the guideline example (1,600 + 12,000 = 13,600).
- **ট্রাক নম্বর** — truck plate, stored on the receipt only. It does not change money.
- **তাৎক্ষণিক পরিশোধ** — how much is paid right now. The rest is unpaid. On a sale the buyer owes the rest. On a purchase you owe the mill the rest.
- **খালি বস্তা প্রদান** — empty sacks handed to the buyer. Added to that buyer's empty-sack count. Not money.
- **নগদ / বিকাশ / নগদ পে / ব্যাংক** — cash, bKash, Nagad, bank. Only নগদ changes the cash box.
- **চালান সংরক্ষণ ও ৪ খাতা আপডেট করুন** — save and update the four books, then show the receipt.

## People `দেনা-পাওনা`

- **সকল খাতা** — everyone.
- **দেনাদার (বাকি পাওনা)** — people marked as buyers, plus anyone whose balance is above zero. A buyer who owes nothing still appears, at zero. Home's market-due card counts only balances above zero.
- **পাওনাদার (মিলার)** — mills. A mill's balance is stored negative. The card shows it as "the mill will get this much."
- **নতুন পার্টি** — add a buyer or a mill. Credit limit `0` means no limit. The empty-sack count is a starting number.
- **কল** — dials the phone.
- **WhatsApp তাগাদা** — opens WhatsApp with a Bangla reminder of what they owe. Shown only when they owe you.
- **বাকি আদায়** — the buyer is paying you. **টাকা দিন** — you are paying a mill. The amount box is the same field either way.
- Payment chips: **নগদ** is paper cash, **বিকাশ** is bKash, **নগদ পে** is Nagad, **ব্যাংক** is the bank. Only নগদ changes the cash box.

## What a fresh install should show

Sample story, already saved:

1. Two days ago, own-rice sale: 30 sacks of Miniket at 3,450 to মেসার্স ভাই ভাই স্টোর. Sale 103,500. Cash paid then 25,000. Unpaid 78,500.
2. Later, that buyer paid another 20,000 in cash. They now owe 58,500.
3. Yesterday, commission sale: 50 sacks of BR-28 at 2,850 to জনতা ট্রেডার্স, rice from সততা রাইস মিল. Sale 142,500. Fee 20 per sack = 1,000. Labor total 500, paid to workers in cash. Mill is owed 141,000. Buyer paid 97,500 by bKash and still owes 45,000.

Home, after that story:

- Sacks on hand: **1,130** (about **1,281** maunds, stock valued at the selling price **৳33,94,000**).
- Today's sales: **৳0** and **0** sacks.
- Money buyers owe you: **৳1,03,500** (58,500 + 45,000).
- Paper cash: **৳44,500** (25,000 + 20,000 − 500). The 97,500 bKash payment is not in this number.

Warehouse:

- মিনিকেট (কুষ্টিয়া স্পেশাল): **250** sacks (opened at 280, sold 30).
- নাজিরশাইল (দিনাজপুর): **160**.
- বিআর-২৮ (নওগাঁ): **300** (opened at 350, sold 50).
- কাটারিভোগ (দিনাজপুর): **90**.
- চিনিগুঁড়া সুগন্ধি (বগুড়া): **120**.
- স্বর্ণা / মোটা চাল: **210**.

People:

- মেসার্স ভাই ভাই স্টোর owes **58,500**. Empty sacks **45** are an opening count, not from the sample invoice.
- জনতা ট্রেডার্স owes **45,000**. Empty sacks **20** are an opening count.
- সততা রাইস মিল (নওগাঁ) is owed **1,41,000**.
- গোল্ডেন অটো রাইস মিল is owed **0**.
- মায়ের দোয়া জেনারেল স্টোর owes **0**. Empty sacks **15** are an opening count.

Cash book:

- Cash in 25,000 from the Miniket sale (paper cash).
- Cash in 20,000 from the later collection (paper cash).
- Cash out 500 for the commission invoice's workers.
- The bKash 97,500 row is in the full list and under **নগদ জমা**, because that chip means money in, not paper notes. It does not change the cash-box number on Home.
