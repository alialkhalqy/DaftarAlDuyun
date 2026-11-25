Accounts Book – Android Project

What This Version Includes

- Native Android application built with Kotlin and Jetpack Compose.
- Local Room database stored on the device.
- Transaction records: cash sales, credit sales, payments received, supplier debts, supplier payments, expenses, income, and configurable obligations (Dhimar).
- Customer and supplier management screens.
- Financial summary and basic aggregated reports.
- Ability to edit default section names and save them locally.
- Linking debt, collection, and payment transactions to customer or supplier IDs.
- Displaying each customer's and supplier's balance and transaction history.
- Due-date entry for credit sales and supplier debts.
- Confirmation prompt before deleting a transaction.
- Ability to edit transaction amounts, names, costs, and notes.
- Exporting transaction records to Excel-compatible CSV files and sharing them.
- Generating basic PDF statements and sharing them through mobile applications.
- No Internet permissions declared in AndroidManifest.

Opening the Project

1. Install Android Studio on your computer.
2. Extract the project archive.
3. Open the "DaftarAlDuyunAndroid" folder in Android Studio.
4. Allow Gradle synchronization to complete.
5. Run the application on an Android device or emulator.

Important Notes

This is a foundational version intended for further development. It is not yet a final commercial release. The APK has not been built, and the project has not been tested on a physical device in this environment.

Before publishing, the following tasks must be completed:

- Complete transaction editing, person information updates, and safe deletion.
- Calculate balances for each party, with support for partial payments and due dates.
- Add transaction editing and deletion with an audit log and protection against data-entry errors.
- Implement accurate date-based filtering for reports.
- Complete PDF and Excel exports and account statement sharing.
- Implement encrypted local backup and restoration.
- Add application protection using a PIN or fingerprint.
- Add unit tests, UI tests, and database schema migration support.
- Prepare the application icon and final signing configuration.

Offline Operation

The application does not request the "INTERNET" permission. All data is stored in a Room database within the application's private storage.

Warning: Uninstalling the application or clearing its data may delete the database. Therefore, local backup functionality must be implemented before relying on the application for commercial use.

---

Phase 4 Update: Backup and Restore

- Added full backup creation in JSON format using the Android file picker.
- Added backup import functionality with validation of file type, version, and essential data.
- Restoration is performed within a single Room transaction, with explicit confirmation before replacing existing data.
- No Internet connection or general storage permissions are required.

Remaining Tasks

- Add application protection using a PIN or fingerprint and database encryption.
- Implement due-date notifications and WhatsApp integration.
- Improve PDF generation to support Arabic text shaping and right-to-left (RTL) layout.
- Add daily, weekly, and monthly reports, and review cash balance, profit, and partial payment calculations.
- The APK has not yet been built, and the project has not been tested on a physical Android device. It must be opened in Android Studio, Gradle synchronization must be completed, and the build must be verified.
- Keep backup files outside the phone. Clearing application data or uninstalling the application may result in the loss of the locally stored database.

---

Visual Identity Update – Splash Screen

- Added a visible application logo featuring a financial wallet inside an identification card, using green and gold colors.
- Displayed the Arabic application name: دفتر حسابات.
- Displayed the English application name: Accounts Book.
- Added the developer credit: برمجة علي حسين الخلقي.
- Added the abbreviated English name to the top application bar as well.
