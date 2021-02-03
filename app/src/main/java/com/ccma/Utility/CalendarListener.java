package com.ccma.Utility;

import java.util.Calendar;

public interface CalendarListener {

    void onFirstDateSelected(Calendar startDate);

    void onDateRangeSelected(Calendar startDate, Calendar endDate);
}
