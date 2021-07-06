package com.utils.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

@SuppressWarnings("unused")
public class DatabaseHelper extends SQLiteOpenHelper {


    private static DatabaseHelper dbhInstance = null;
    private static Context context;
    private static final String DATABASE_NAME = "mydatabase.db";
    private static final int DATABASE_VERSION = 1;

    public static final String[] TABLE_NAMES =
            {
                "button", "modifier", "modifiers_group", "modifiers_assigned", "modifiers_group_assigned", "vat",
                "bill_total", "product_bill", "modifier_bill", "payment_option_button", "bill_subdivision_paid",
                "item_subdivisions", "sessions", "customer_bill", "product_unspec_bill", "modifiers_bill_notes", "bill_total_credit", "bill_total_extra",
                "client", "company", "client_in_company", "user",
                "temp_table", "last_session",
                "room", "table_configuration", "table_use", "table_use_extension", "discount", "discount_mode", "reservation", "waiting_list",
                "fidelity", "fiscal_printer", "kitchen_printer"
            };


    private static final String FISCAL_PRINTER_CREATE = "CREATE TABLE fiscal_printer(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT, " +
            "address TEXT, " +
            "model TEXT,"+
            "port INTEGER,"+
            "api INTEGER DEFAULT 0); ";

    private static final String KITCHEN_PRINTER_CREATE = "CREATE TABLE kitchen_printer(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT, " +
            "port INTEGER , " +
            "single_order INTEGER DEFAULT 0,"+
            "address TEXT ); ";

    private static final String BLACKBOX_INFO_CREATE = "CREATE TABLE blackbox_info(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT default 'blackbox', " +
            "address TEXT ); ";

    private static final String STATIC_ACTIVATION_CODE_CREATE = "CREATE TABLE static_activation_code (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "code TEXT, " +
            "duration INTEGER,"+
            "position INTEGER,"+
            "used INTEGER DEFAULT 0); ";

    private static final String REGISTERED_ACTIVATION_CODE_CREATE = "CREATE TABLE registered_activation_code(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "code TEXT, registration TEXT); ";


    // SQL creation statement
    //1
    private static final String BUTTON_CREATE = "CREATE TABLE button (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT, " +
            "subtitle TEXT, " +
            "img_name TEXT, " +
            "color INTEGER, " +
            "position INTEGER, " +
            "price FLOAT, " +
            "vat INTEGER DEFAULT 0, " +
            "barcode TEXT," +
            "printer INTEGER DEFAULT -1,"+
            "productCode TEXT, catID INTEGER, isCat BOOLEAN, " +
            "fidelity_discount INTEGER DEFAULT 0,"+
            "fidelity_credit INTEGER DEFAULT 0,"+
            "credit_value INTEGER DEFAULT 0,"+
            "FOREIGN KEY(vat) REFERENCES vat(value));";

    //2
    String UNIT_CREATE = "CREATE TABLE unit(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "description TEXT);";

    //3
    String INGRIDIENT_CREATE = "CREATE TABLE ingridients(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "description TEXT," +
            "unit_id INT ,"+
            "FOREIGN KEY(unit_id) REFERENCES unit(id));";

    String BUTTON_RECIPE_CREATE = "CREATE TABLE button_recipe (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "product_id INT, " +
            "ingridient_id INT, " +
            "quantity INT, " +
            "unit INT, " +
            "FOREIGN KEY(product_id) REFERENCES button(id),"+
            "FOREIGN KEY(ingridient_id) REFERENCES ingridients(id));";

    String MODIFIER_RECIPE_CREATE = "CREATE TABLE modifier_recipe (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "modifier_id INT, " +
            "ingridient_id INT, " +
            "quantity INT, " +
            "unit INT, " +
            "FOREIGN KEY(modifier_id) REFERENCES modifier(id),"+
            "FOREIGN KEY(ingridient_id) REFERENCES ingridients(id));";

    //add
    private static final String MODIFIERS_CREATE = "CREATE TABLE modifier (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT, " +
            "position INTEGER, " +
            "price FLOAT, " +
            "vat INTEGER DEFAULT 0, " +
            "groupID INTEGER, " +
            "FOREIGN KEY(vat) REFERENCES vat(value));";

    //add
    private static final String MODIFIERS_GROUP_CREATE = "CREATE TABLE modifiers_group (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "title TEXT, position INTEGER, notes integer);";

    //add
    private static final String MODIFIERS_GROUP_ASSIGNED_CREATE = "CREATE TABLE modifiers_group_assigned (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "prod_id INTEGER, group_id INTEGER, all_the_group BOOLEAN, " +
            "fixed INTEGER, " +
            "FOREIGN KEY(prod_id) REFERENCES button(id), " +
            "FOREIGN KEY(group_id) REFERENCES modifiers_group(id));";

    //add
    private static final String MODIFIERS_ASSIGNED_CREATE = "CREATE TABLE modifiers_assigned (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "assignment_id INTEGER, modifier_id INTEGER," +
            "fixed INTEGER , " +
            "FOREIGN KEY(assignment_id) REFERENCES modifiers_group_assigned(id), " +
            "FOREIGN KEY(modifier_id) REFERENCES modifier(id));";

    //add
    private static final String USER_CREATE = "CREATE TABLE user (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "name TEXT, " +
            "surname TEXT, " +
            "email TEXT , " +
            "password TEXT, " +
            "userType INTEGER, " +
            "passcode TEXT)";


    /**
     * 0 NOT YET PAID
     * 1 PAID
     * 2 DELETED ORDER
     * 3 DELETED UNPAID
     * 4 UNPAID
     * 5 PARTIAL PAID
     * 9 WHITEOUT
     */
    //add
    private static final String TOTAL_BILL_COSTUMER_INVOICE_CREATE = "CREATE TABLE bill_total_customer_invoice (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "bill_total_id INTEGER, " +
            "client_id INTEGER, " +
            "FOREIGN KEY(bill_total_id) REFERENCES bill_total(id),"+
            "FOREIGN KEY(client_id) REFERENCES client(id));";
    //add

    private static final String TOTAL_BILL_CREATE = "CREATE TABLE bill_total (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "total FLOAT, " +
            "paid INTEGER, " +
            "creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "pay_time  TIMESTAMP DEFAULT null," +
            "bill_number INTEGER,"+
            "payment_type INTEGER," +
            "invoice INTEGER DEFAULT 0," +
            "print_index INTEGER DEFAULT 0," +
            "android_id TEXT);";

    //add
    private static final String CUSTOMER_BILL_CREATE = "CREATE TABLE customer_bill (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "position INTEGER, " +
            "description TEXT, " +
            "client_id INTEGER DEFAULT 0, " +
            "prod_bill_id INTEGER, " +
            "FOREIGN KEY(prod_bill_id) REFERENCES product_bill(id));";

    //add
    private static final String PRODUCT_BILL_CREATE= "CREATE TABLE product_bill (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "position INTEGER, " +
            "prod_id INTEGER, " +
            "qty INTEGER, " +
            "bill_id INTEGER, " +
            "homage INTEGER DEFAULT 0," +
            "discount FLOAT DEFAULT 0,"+
            "course INTEGER DEFAULT 0,"+
            "FOREIGN KEY(bill_id) REFERENCES bill_total(id), " +
            "FOREIGN KEY(prod_id) REFERENCES button(id));";

    //add
    private static final String PRODUCT_BILL_UNSPECIFIC_CREATE= "CREATE TABLE product_unspec_bill (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "prod_bill_id INTEGER, " +
            "price FLOAT, " +
            "description TEXT, " +
            "FOREIGN KEY(prod_bill_id) REFERENCES product_bill(id));";

    //add
    private static final String MODIFIER_BILL_CREATE= "CREATE TABLE modifier_bill (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "position INTEGER, " +
            "mod_id INTEGER, " +
            "qty INTEGER, " +
            "prod_bill_id INTEGER, " +
            "homage INTEGER DEFAULT 0," +
            "discount FLOAT DEFAULT 0,"+
            "FOREIGN KEY(prod_bill_id) REFERENCES product_bill(id), " +
            "FOREIGN KEY(mod_id) REFERENCES modifier(id));";

    //add
    private static final String MODIFIER_BILL_NOTES_CREATE= "CREATE TABLE modifier_bill_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "modifier_bill_id INTEGER, " +
            "note TEXT, " +
            "FOREIGN KEY(modifier_bill_id) REFERENCES modifier_bill(id));";

    //add
    private static final String TOTAL_BILL_CREDIT_CREATE= "CREATE TABLE bill_total_credit (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "creditValue FLOAT, " +
            "bill_total_id INTEGER, "+
            "FOREIGN KEY(bill_total_id) REFERENCES bill_total(id));";

    //add
    private static final String TOTAL_BILL_EXTRA_CREATE = "CREATE TABLE bill_total_extra (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "discountTotal FLOAT, "+
            "homage INTEGER, " +
            "bill_total_id INTEGER, "+
            "FOREIGN KEY(bill_total_id) REFERENCES bill_total(id));";

    //payment type cash 1, credit card 2, debt card 3, ticket 4
    private static final String BILL_SUBDIVISION_PAID_CREATE = "CREATE TABLE bill_subdivision_paid (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "bill_id INTEGER, " +
            "subdivision_mode INTEGER, " +
            "subdivision_value FLOAT, " +
            "paid_amount FLOAT, " +
            "payment_type INT,"+
            "discount FLOAT,"+
            "homage INT,"+
            "invoice INTEGER DEFAULT 0," +
            "FOREIGN KEY(bill_id) REFERENCES bill_total(id)); ";

    private static final String ITEM_SUBDIVISIONS_CREATE = "CREATE TABLE item_subdivisions (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "bill_subdivision_id INTEGER, " +
            "product_bill_id INTEGER, " +
            "quantity INTEGER, " +
            " discount FLOAT, " +
            " percentage INT DEFAULT 1," +
            " price DECIMAL ," +
            "FOREIGN KEY(bill_subdivision_id) REFERENCES bill_subdivision_paid(id), " +
            "FOREIGN KEY(product_bill_id) REFERENCES product_bill(id));";

    private static final String ITEM_PAID_SPEC_CREATE = "CREATE TABLE item_paid_spec (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "paid_amount FLOAT, " +
            "payment_type INTEGER, " +
            "bill_subdivision_paid_id INTEGER, " +
            "FOREIGN KEY(bill_subdivision_paid_id) REFERENCES bill_subdivision_paid(id));";
    //add
    private static final String PAYMENT_OPTION_BUTTON_CREATE = "CREATE TABLE payment_option_button (id INTEGER PRIMARY KEY, " +
            "parent_id INTEGER, " +
            "button_type INTEGER, " +
            "button_title TEXT);";

    //add
    private static final String CLIENT_CREATE = "CREATE TABLE client (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "surname TEXT, " +
            "email TEXT);";

    //add
    private static final String COMPANY_CREATE = "CREATE TABLE company (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "company_name TEXT, " +
            "address TEXT, " +
            "vat_number TEXT, " +
            "postal_code TEXT, " +
            "city TEXT, " +
            "country TEXT,"+
            "codice_fiscale TEXT," +
            "provincia TEXT,"+
            "codice_destinatario TEXT, " +
            "pec TEXT);";

    //add
    private static final String CLIENT_IN_COMPANY_CREATE = "CREATE TABLE client_in_company (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "client_id INTEGER, " +
            "company_id INTEGER, " +
            "orders_made INTEGER, " +
            "FOREIGN KEY(client_id) REFERENCES client(id));";

    //no more
    private static final String TEMP_TABLE_CREATE= "CREATE TABLE temp_table (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "table_number INTEGER, " +
            "total_bill_id INTEGER,"  +
            "FOREIGN KEY(total_bill_id) REFERENCES bill_total(id));";

    //add
    private static final String LAST_SESSION_CREATE= "CREATE TABLE last_session(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "last_session_creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "position INTEGER);";

    //
    private static final String SESSION_TABLE_CREATE= "CREATE TABLE sessions(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "sessionName TEXT, "+
            "start TEXT," +
            "endTime TEXT);";


    private static final String VAT_TABLE_CREATE= "CREATE TABLE vat(id INTEGER PRIMARY KEY AUTOINCREMENT, value INTEGER, perc FLOAT);";


    private static final String ROOM_TABLE_CREATE = "CREATE TABLE room ( id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "name TEXT, " +
            "start_time TIMESTAMP, "+
            "end_time TIMESTAMP, "+
            "season_start TIMESTAMP, "+
            "season_end TIMESTAMP);";

    private static final String TABLE_CONFIGURATION_CREATE = "CREATE TABLE table_configuration ( id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "table_number INTEGER," +
            "seat_number INTEGER," +
            "table_name TEXT," +
            "merge_table INTEGER," +
            "share_table INTEGER," +
            "room_id INTEGER," +
            "FOREIGN KEY (room_id) REFERENCES room(id));";

    private static final String TABLE_USE_CREATE = "CREATE TABLE table_use ( id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "table_id INTEGER, " +
            "total_seat INTEGER," +
            "start_time TIMESTAMP," +
            "end_time TIMESTAMP," +
            "total_bill_id INTEGER,"+
            "main_table INTEGER," +
            "main_table_number INTEGER,"+
            "FOREIGN KEY (total_bill_id) REFERENCES bill_total(id),"+
            "FOREIGN KEY (table_id) REFERENCES table_configuration(id));";

    private static final String TABLE_USE_EXT_CREATE = "CREATE TABLE table_use_extension ( id INTEGER PRIMARY KEY AUTOINCREMENT, "+
            "table_use_id INTEGER, " +
            "table_id  INTEGER," +
            "seat_number INTEGER," +
            "FOREIGN KEY (table_use_id) REFERENCES table_use(id),"+
            "FOREIGN KEY (table_id) REFERENCES table_configuration(id));";

    private static final String DISCOUNT_TABLE_CREATE = "CREATE TABLE discount (id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "client_id INTEGER, " +
            "discount_mode_id TEXT, " +
            "FOREIGN KEY (client_id) REFERENCES client(id), " +
            "FOREIGN KEY (discount_mode_id) REFERENCES discount_mode(description));";


    private static final String DEVICE_INFO_TABLE_CREATE = "CREATE TABLE device_info(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "ragione_sociale TEXT, " +
            "partita_iva TEXT, " +
            "address TEXT, " +
            "cap TEXT, " +
            "comune TEXT, " +
            "provincia TEXT, " +
            "email TEXT, " +
            "android_id TEXT, " +
            "token_id TEXT, " +
            "ip TEXT," +
            "multicast_ip TEXT,"+
            "master INT, "+
            "store_name TEXT, "+
            "online_check INTEGER);";

    /**
     * name = nome dato alla stampante
     * code = mappa delle stampanti supportate (ditro=1, hprt=2)...
     * type = 0/1 fiscal/ non fiscal
     * IP = ip assegnato alla stampante
     */
   /* private static final String PRINTER_INFO_TABLE_CREATE = "CREATE TABLE printer_info(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT, " +
            "code INTEGER, " +
            "type INTEGER, " +
            "IP TEXT);";*/

    /**
     *MODE:
     * 0 CLIENT DISCOUNT
     * 1 PROMOTION
     */
    private static final String DISCOUNT_MODE_TABLE_CREATE = "CREATE TABLE discount_mode (description TEXT PRIMARY KEY, " +
            "value INTEGER, " +
            "mode INTEGER);";

    /**
     * STATUS:
     * 0 WAITING
     * 1 NOT ARRIVED IN TIME
     * 2 CANCELLED
     * 3 ARRIVED/PAYED
     */
    /**
     * TIMESTRING can be manipulated with SQLite date(), time(), datetime(), strftime() methods
     */
    private static final String RESERVATION_TABLE_CREATE = "CREATE TABLE reservation (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "surname TEXT, " +
            "adults INTEGER, " +
            "children INTEGER, " +
            "disabled INTEGER, " +
            "date TIMESTRING, " +
            "time TIMESTRING, " +
            "telephone INTEGER);";

    private static final String WAITING_LIST_TABLE_CREATE = "CREATE TABLE waiting_list (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "surname TEXT, " +
            "time TIMESTAMP, " +
            "adults INTEGER, " +
            "children INTEGER, " +
            "disabled INTEGER);";


    private static final String CASH_MANAGEMENT_SET_CREATE = "CREATE TABLE cash_management_set(min_cash FLOAT, " +
            "max_cash FLOAT, " +
            "min_withdraw FLOAT, " +
            "five_cents INTEGER, " +
            "ten_cents INTEGER, " +
            "twenty_cents INTEGER, " +
            "fifty_cents INTEGER, " +
            "one_euros INTEGER, " +
            "two_euros INTEGER, " +
            "five_euros INTEGER, " +
            "ten_euros INTEGER, " +
            "twenty_euros INTEGER, " +
            "fifty_euros INTEGER, " +
            "hundred_euros INTEGER, " +
            "two_hundred_euros INTEGER);";

    private static final String CASH_MANAGEMENT_REAL_CREATE = "CREATE TABLE cash_management_real(min_cash FLOAT, " +
            "max_cash FLOAT, " +
            "min_withdraw FLOAT, " +
            "current_total FLOAT, " +
            "five_cents INTEGER, " +
            "ten_cents INTEGER, " +
            "twenty_cents INTEGER, " +
            "fifty_cents INTEGER, " +
            "one_euros INTEGER, " +
            "two_euros INTEGER, " +
            "five_euros INTEGER, " +
            "ten_euros INTEGER, " +
            "twenty_euros INTEGER, " +
            "fifty_euros INTEGER, " +
            "hundred_euros INTEGER, " +
            "two_hundred_euros INTEGER);";

    /**
     * APEX Automatic Process EXecutor
     */
    private static final String WORK_STATION_TABLE_CREATE = "CREATE TABLE work_station (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name_station TEXT, " +
            "max_station_item INT, " +
            "display_name TEXT );";

    private static final String INVENTORY_INGRIDIENTS_TABLE_CREATE = "CREATE TABLE inventory_ingridients(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "ingridient_max INT, "+
            "ingridient_min INT,"+
            "ingridient_unit INT, "+
            "ingridient_quantity INT,"+
            "ingridient_id INT, "+
            "work_station_id INT,"+
            "FOREIGN KEY (ingridient_id) REFERENCES ingridients(id),"+
            "FOREIGN KEY (work_station_id) REFERENCES work_station(id));";

    private static final String INVENTORY_PRODUCTS_TABLE_CREATE = "CREATE TABLE inventory_products(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "product_max INT, "+
            "product_min INT,"+
            "product_unit INT, "+
            "product_quantity INT,"+
            "ingridient_id INT, "+
            "work_station_id INT,"+
            "FOREIGN KEY (ingridient_id) REFERENCES ingridients(id),"+
            "FOREIGN KEY (work_station_id) REFERENCES work_station(id));";

    private static final String ORDER_PROCESS_TABLE_CREATE = "CREATE TABLE order_process (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name_process TEXT, " +
            "max_process_items INT, "+
            "min_process_items INT,"+
            "optimize_process INT, "+
            "display_id INT,"+
            "count_item INT, "+
            "work_station_id INT,"+
            "FOREIGN KEY (work_station_id) REFERENCES work_station(id));";

    private static final String PROCESS_STEP_TABLE_CREATE = "CREATE TABLE process_steps(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "ingridient_unit INT, "+
            "category_step INT,"+
            "process_id INT,"+
            "ingridient_id INT,"+
            "FOREIGN KEY (process_id) REFERENCES order_process(id),"+
            "FOREIGN KEY (ingridient_id) REFERENCES ingridients(id));";

    private static final String ITEM_PRODUCTS_TABLE_CREATE = "CREATE TABLE item_products(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "ingridient_unit INT, "+
            "category_step INT,"+
            "process_id INT,"+
            "product_id INT,"+
            "FOREIGN KEY (process_id) REFERENCES order_process(id),"+
            "FOREIGN KEY (category_step) REFERENCES process_steps(id),"+
            "FOREIGN KEY (product_id) REFERENCES button(id));";

    private static final String ITEM_INGRIDIENTS_TABLE_CREATE = "CREATE TABLE item_ingridients(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "ingridient_unit INT, "+
            "category_step INT,"+
            "process_id INT,"+
            "ingridient_id INT,"+
            "FOREIGN KEY (process_id) REFERENCES order_process(id),"+
            "FOREIGN KEY (category_step) REFERENCES process_steps(id),"+
            "FOREIGN KEY (ingridient_id) REFERENCES ingridients(id));";

    private static final String FOCUS_FILTERS_TABLE_CREATE = "CREATE TABLE focus_filters(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "count_single_item INT, "+
            "divide_item_variaton INT,"+
            "show_order_item INT,"+
            "process_step INT,"+
            "FOREIGN KEY (process_step) REFERENCES process_steps(id));";

    private static final String STEP_FUNCTIONS_TABLE_CREATE = "CREATE TABLE step_functions(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "print_item INT, "+
            "order_served INT,"+
            "clear_table INT,"+
            "subtract_items INT,"+
            "table_number INT,"+
            "process_step INT,"+
            "FOREIGN KEY (process_step) REFERENCES process_steps(id));";

    private static final String ALERT_TABLE_CREATE = "CREATE TABLE alert(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "description TEXT); ";

    private static final String STEP_TIMERS_TABLE_CREATE = "CREATE TABLE step_timers(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "set_time TIMESTAMP, "+
            "alert_time TIMESTAMP,"+
            "alert_id INT,"+
            "process_step INT,"+
            "FOREIGN KEY (alert_id) REFERENCES alert(id),"+
            "FOREIGN KEY (process_step) REFERENCES process_steps(id));";

    private static final String STEP_VISUALIZATION_TABLE_CREATE = "CREATE TABLE step_visualization(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "select_visualization INT, "+
            "visualization_priority INT,"+
            "display_id INT,"+
            "process_step INT,"+
            "FOREIGN KEY (process_step) REFERENCES process_steps(id));";


    private static final String FISCAL_CLOSE_CREATE = "CREATE TABLE fiscal_close( last_close TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    private static final String LAST_UPDATE_CREATE = "CREATE TABLE last_update( last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    private static final String LOGIN_RECORD_CREATE = "CREATE TABLE login_record( username TEXT, login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

    //private static final String NUMERO_FATTURA_CREATE = "CREATE TABLE numero_fattura( numero_fattura INT DEFAULT 0);";

    private static final String NUMERO_FATTURA_CREATE = "CREATE TABLE fattura( numero_fattura INT DEFAULT 0);";

    private static final String GENERAL_SETTINGS_TABLE_CREATE = "CREATE TABLE general_settings(reservation_timer INT DEFAULT 1);";

    /**
     * STATISTIC TABLE PART
     */
    //add
    private static final String TOTAL_BILL_COSTUMER_INVOICE_CREATE_STATISTIC = "CREATE TABLE bill_total_customer_invoice_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "bill_total_id INTEGER, " +
            "client_id INTEGER, " +
            "FOREIGN KEY(bill_total_id) REFERENCES bill_total_statistic(id),"+
            "FOREIGN KEY(client_id) REFERENCES client(id));";
    //add

    private static final String TOTAL_BILL_CREATE_STATISTIC = "CREATE TABLE bill_total_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "total FLOAT, " +
            "paid INTEGER, " +
            "creation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "pay_time  TIMESTAMP DEFAULT null," +
            "bill_number INTEGER,"+
            "payment_type INTEGER," +
            "invoice INTEGER DEFAULT 0," +
            "print_index INTEGER DEFAULT 0);";

    //add
    private static final String COSTUMER_BILL_CREATE_STATISTIC = "CREATE TABLE customer_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "position INTEGER, " +
            "description TEXT, " +
            "client_id INTEGER DEFAULT 0, " +
            "prod_bill_id INTEGER, " +
            "FOREIGN KEY(prod_bill_id) REFERENCES product_bill_statistic(id));";

    //add
    private static final String PRODUCT_BILL_CREATE_STATISTIC= "CREATE TABLE product_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "position INTEGER, " +
            "prod_id INTEGER, " +
            "qty INTEGER, " +
            "bill_id INTEGER, " +
            "homage INTEGER DEFAULT 0," +
            "discount FLOAT DEFAULT 0,"+
            "FOREIGN KEY(bill_id) REFERENCES bill_total_statistic(id), " +
            "FOREIGN KEY(prod_id) REFERENCES button(id));";

    //add
    private static final String PRODUCT_BILL_UNSPECIFIC_CREATE_STATISTIC= "CREATE TABLE product_unspec_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "prod_bill_id INTEGER, " +
            "price FLOAT, " +
            "description TEXT, " +
            "FOREIGN KEY(prod_bill_id) REFERENCES product_bill_statistic(id));";

    //add
    private static final String MODIFIER_BILL_CREATE_STATISTIC= "CREATE TABLE modifier_bill_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "position INTEGER, " +
            "mod_id INTEGER, " +
            "qty INTEGER, " +
            "prod_bill_id INTEGER, " +
            "homage INTEGER DEFAULT 0," +
            "discount FLOAT DEFAULT 0,"+
            "FOREIGN KEY(prod_bill_id) REFERENCES product_bill_statistic(id), " +
            "FOREIGN KEY(mod_id) REFERENCES modifier(id));";

    //add
    private static final String MODIFIER_BILL_NOTES_CREATE_STATISTIC= "CREATE TABLE modifier_bill_notes_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "modifier_bill_id INTEGER, " +
            "note TEXT, " +
            "FOREIGN KEY(modifier_bill_id) REFERENCES modifier_bill_statistic(id));";

    //add
    private static final String TOTAL_BILL_CREDIT_CREATE_STATISTIC= "CREATE TABLE bill_total_credit_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "creditValue FLOAT, " +
            "bill_total_id INTEGER, "+
            "FOREIGN KEY(bill_total_id) REFERENCES bill_total_statistic(id));";

    //add
    private static final String TOTAL_BILL_EXTRA_CREATE_STATISTIC = "CREATE TABLE bill_total_extra_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "discountTotal FLOAT, "+
            "homage INTEGER, " +
            "bill_total_id INTEGER, "+
            "FOREIGN KEY(bill_total_id) REFERENCES bill_total_statistic(id));";

    //payment type cash 1, credit card 2, debt card 3, ticket 4
    private static final String BILL_SUBDIVISION_PAID_CREATE_STATISTIC = "CREATE TABLE bill_subdivision_paid_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "bill_id INTEGER, " +
            "subdivision_mode INTEGER, " +
            "subdivision_value FLOAT, " +
            "paid_amount FLOAT, " +
            "payment_type INT,"+
            "discount FLOAT,"+
            "homage INT,"+
            "invoice INTEGER DEFAULT 0," +
            "FOREIGN KEY(bill_id) REFERENCES bill_total_statistic(id)); ";

    private static final String ITEM_SUBDIVISIONS_CREATE_STATISTIC = "CREATE TABLE item_subdivisions_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "bill_subdivision_id INTEGER, " +
            "product_bill_id INTEGER, " +
            "quantity INTEGER, " +
            " discount FLOAT, " +
            " percentage INT DEFAULT 1," +
            " price DECIMAL ," +
            "FOREIGN KEY(bill_subdivision_id) REFERENCES bill_subdivision_paid_statistic(id), " +
            "FOREIGN KEY(product_bill_id) REFERENCES product_bill_statistic(id));";

    private static final String ITEM_PAID_SPEC_CREATE_STATISTIC = "CREATE TABLE item_paid_spec_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "paid_amount FLOAT, " +
            "payment_type INTEGER, " +
            "bill_subdivision_paid_id INTEGER, " +
            "FOREIGN KEY(bill_subdivision_paid_id) REFERENCES bill_subdivision_paid_statistic(id));";


    private static final String DROP_BILL_TABLE= "DROP TABLE total_bill";
    private static final String DROP_BILL_PRODUCT_TABLE= "DROP TABLE product_bill";
    private static final String DROP_BILL_MODIFIER_TABLE= "DROP TABLE modifier_bill";


    private static final String INSTALL_STATUS_CREATE  = "CREATE TABLE install_status (installed INTEGER DEFAULT 0);";
    private static final String INSTALL_STATUS_INIT = "INSERT INTO install_status(installed) values(0);";


    private static final String FIDELITY_CREATE = "CREATE TABLE fidelity(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "code TEXT, " +
            "rule INTEGER, " +
            "active INTEGER,"+
            "earned DOUBLE,"+
            "used DOUBLE,"+
            "value DOUBLE ); ";





    // create a table to store the checksum of each.
    // This table will be updated on every update with the blackbox,
    // in order to know if a table is synced with the blackbox DB
    private static final String CHECKSUM_CREATE = "CREATE TABLE checksum_registry (name TEXT, Checksum TEXT);";







    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }



    public static synchronized DatabaseHelper getInstance(Context context){
        if(dbhInstance == null){
            dbhInstance = new DatabaseHelper(context.getApplicationContext());
        }

        return dbhInstance;
    }

    // Called on db creation
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(FISCAL_PRINTER_CREATE);
        database.execSQL(KITCHEN_PRINTER_CREATE);
        database.execSQL(BLACKBOX_INFO_CREATE);
        database.execSQL(STATIC_ACTIVATION_CODE_CREATE);
        database.execSQL(REGISTERED_ACTIVATION_CODE_CREATE);
        database.execSQL(DEVICE_INFO_TABLE_CREATE);
        database.execSQL(BUTTON_CREATE);
        database.execSQL(MODIFIERS_CREATE);
        database.execSQL(MODIFIERS_GROUP_CREATE);
        database.execSQL(MODIFIERS_GROUP_ASSIGNED_CREATE);
        database.execSQL(MODIFIERS_ASSIGNED_CREATE);
        database.execSQL(USER_CREATE);
        database.execSQL(TOTAL_BILL_CREATE);
        database.execSQL(TOTAL_BILL_EXTRA_CREATE);
        database.execSQL(PRODUCT_BILL_CREATE);
        database.execSQL(CUSTOMER_BILL_CREATE);
        database.execSQL(PRODUCT_BILL_UNSPECIFIC_CREATE);
        database.execSQL(MODIFIER_BILL_CREATE);
        database.execSQL(MODIFIER_BILL_NOTES_CREATE);
        database.execSQL(TOTAL_BILL_CREDIT_CREATE);
        database.execSQL(PAYMENT_OPTION_BUTTON_CREATE);
        database.execSQL(CLIENT_CREATE);
        database.execSQL(COMPANY_CREATE);
        database.execSQL(CLIENT_IN_COMPANY_CREATE);
        database.execSQL(TEMP_TABLE_CREATE);
        database.execSQL(LAST_SESSION_CREATE);
        database.execSQL(BILL_SUBDIVISION_PAID_CREATE);
        database.execSQL(ITEM_SUBDIVISIONS_CREATE);
        database.execSQL(ITEM_PAID_SPEC_CREATE);
        database.execSQL(SESSION_TABLE_CREATE);
        database.execSQL(VAT_TABLE_CREATE);
        database.execSQL(ROOM_TABLE_CREATE);
        database.execSQL(TABLE_CONFIGURATION_CREATE);
        database.execSQL(TABLE_USE_CREATE);
        database.execSQL(TABLE_USE_EXT_CREATE);
        database.execSQL(DISCOUNT_TABLE_CREATE);
        database.execSQL(DISCOUNT_MODE_TABLE_CREATE);
        database.execSQL(RESERVATION_TABLE_CREATE);
        database.execSQL(WAITING_LIST_TABLE_CREATE);

        database.execSQL(WORK_STATION_TABLE_CREATE);
        database.execSQL(INVENTORY_INGRIDIENTS_TABLE_CREATE);
        database.execSQL(INVENTORY_PRODUCTS_TABLE_CREATE);
        database.execSQL(ORDER_PROCESS_TABLE_CREATE);
        database.execSQL(PROCESS_STEP_TABLE_CREATE);
        database.execSQL(ITEM_PRODUCTS_TABLE_CREATE);
        database.execSQL(ITEM_INGRIDIENTS_TABLE_CREATE);
        database.execSQL(FOCUS_FILTERS_TABLE_CREATE);
        database.execSQL(STEP_FUNCTIONS_TABLE_CREATE);
        database.execSQL(STEP_VISUALIZATION_TABLE_CREATE);
        database.execSQL(ALERT_TABLE_CREATE);
        database.execSQL(STEP_TIMERS_TABLE_CREATE);
        database.execSQL(FISCAL_CLOSE_CREATE);
        database.execSQL(LAST_UPDATE_CREATE);
        database.execSQL(LOGIN_RECORD_CREATE);
        database.execSQL(NUMERO_FATTURA_CREATE);
        database.execSQL(UNIT_CREATE);
        database.execSQL(INGRIDIENT_CREATE);
        database.execSQL(BUTTON_RECIPE_CREATE);
        database.execSQL(MODIFIER_RECIPE_CREATE);
        database.execSQL(TOTAL_BILL_COSTUMER_INVOICE_CREATE);

        database.execSQL(TOTAL_BILL_CREATE_STATISTIC);
        database.execSQL(TOTAL_BILL_EXTRA_CREATE_STATISTIC);
        database.execSQL(PRODUCT_BILL_CREATE_STATISTIC);
        database.execSQL(COSTUMER_BILL_CREATE_STATISTIC);
        database.execSQL(PRODUCT_BILL_UNSPECIFIC_CREATE_STATISTIC);
        database.execSQL(MODIFIER_BILL_CREATE_STATISTIC);
        database.execSQL(MODIFIER_BILL_NOTES_CREATE_STATISTIC);
        database.execSQL(TOTAL_BILL_CREDIT_CREATE_STATISTIC);
        database.execSQL(BILL_SUBDIVISION_PAID_CREATE_STATISTIC);
        database.execSQL(ITEM_SUBDIVISIONS_CREATE_STATISTIC);
        database.execSQL(ITEM_PAID_SPEC_CREATE_STATISTIC);
        database.execSQL(TOTAL_BILL_COSTUMER_INVOICE_CREATE_STATISTIC);

        database.execSQL(GENERAL_SETTINGS_TABLE_CREATE);

        database.execSQL(INSTALL_STATUS_CREATE);
        database.execSQL(INSTALL_STATUS_INIT);

        database.execSQL(FIDELITY_CREATE);



        // create the checksum table
        database.execSQL(CHECKSUM_CREATE);

        // and insert a placeholder value for each table present in the database
        for (String name : TABLE_NAMES)
            { database.execSQL(String.format("\nINSERT INTO checksum_registry (name, checksum) VALUES ('%s', '%s');", name, "00")); }



    }


    // Method called on db update
    @Override
    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
        for (String s: TABLE_NAMES) database.execSQL("DROP TABLE IF EXISTS " + s);

        onCreate(database);
    }






    public static void createBill(SQLiteDatabase database) {
        database.execSQL(TOTAL_BILL_CREATE);
        database.execSQL(PRODUCT_BILL_CREATE);
        database.execSQL(MODIFIER_BILL_CREATE);
    }


    public static void dropBill(SQLiteDatabase database){
        database.execSQL(DROP_BILL_TABLE);
        database.execSQL(DROP_BILL_PRODUCT_TABLE);
        database.execSQL(DROP_BILL_MODIFIER_TABLE);
    }

    public void ciccioPasticcio(SQLiteDatabase database){
        database.execSQL(CLIENT_IN_COMPANY_CREATE);
        database.execSQL(CLIENT_CREATE);
        database.execSQL(COMPANY_CREATE);
    }

    public void createVatTable(SQLiteDatabase database){
        database.execSQL(VAT_TABLE_CREATE);
    }

    public void createDiscountTables(SQLiteDatabase database){
        database.execSQL(DISCOUNT_TABLE_CREATE);
        database.execSQL(DISCOUNT_MODE_TABLE_CREATE);}


    //method to close old db and load new one, got from server
    public boolean importDatabase(String newPath, String oldPath) throws IOException {
        close();

        File newDB = new File(newPath);
        File currentDB = new File(oldPath);
        if(newDB.exists()){
            FileInputStream newDBstream = new FileInputStream(newDB);
            FileOutputStream currentDBstream = new FileOutputStream(currentDB);

            FileChannel fromChannel = null;
            FileChannel toChannel = null;

            try{
                fromChannel = newDBstream.getChannel();
                toChannel = currentDBstream.getChannel();
                fromChannel.transferTo(0, fromChannel.size(), toChannel);
            } finally {
                try{
                    if(fromChannel != null)
                        fromChannel.close();
                } finally {
                    if(toChannel != null)
                        toChannel.close();
                }
            }

            getWritableDatabase().close();
            return true;
        }

        return false;
    }

}
