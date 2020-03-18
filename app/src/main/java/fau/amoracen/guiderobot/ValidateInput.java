package fau.amoracen.guiderobot;

import android.content.Context;
import android.util.Patterns;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Validate all types of inputs
 */
class ValidateInput {
    private static ValidateInput instance;
    private HashMap<String, String> hmap;

    synchronized static ValidateInput getInstance() {
        if (instance == null) {
            instance = new ValidateInput();
        }
        return instance;
    }

    /**
     * Check Email
     *
     * @return true if a valid email was entered, false otherwise
     */
    HashMap validateEmail(Context context, String input) {
        hmap = new HashMap<>();
        if (input.isEmpty()) {
            hmap.put("message", getStringResource(context, R.string.email_cannot_be_empty));
            return hmap;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            hmap.put("message", getStringResource(context, R.string.email_not_valid));
            return hmap;
        }
        hmap.put("message", "Valid");
        return hmap;
    }

    /**
     * Check Name.
     *
     * @return true if  name is valid
     */
    HashMap validateName(Context context, String input) {
        hmap = new HashMap<>();
        if (input.isEmpty()) {
            hmap.put("message", getStringResource(context, R.string.field_cannot_be_empty));
            return hmap;
        } else if (input.length() > 25) {
            hmap.put("message", getStringResource(context, R.string.name_long));
            return hmap;
        }
        hmap.put("message", "Valid");
        return hmap;
    }

    /**
     * Check Password
     *
     * @param input a string
     * @return true if password is valid
     */
    HashMap validatePassword(Context context, String input) {
        hmap = new HashMap<>();
        Pattern anyLetter = Pattern.compile("(?=.*[a-zA-Z])");
        Pattern oneDigit = Pattern.compile("(?=.*[0-9])");
        Pattern noWhiteSpace = Pattern.compile("\\s");
        Pattern totalCharacters = Pattern.compile(".{6,}");
        /*Checking Password*/
        if (input.isEmpty()) {
            hmap.put("message", getStringResource(context, R.string.field_cannot_be_empty));
            return hmap;
        } else if (!anyLetter.matcher(input).find()) {
            hmap.put("message", getStringResource(context, R.string.password_letters));
            return hmap;
        } else if (!oneDigit.matcher(input).find()) {
            hmap.put("message", getStringResource(context, R.string.password_digits));
            return hmap;
        } else if (noWhiteSpace.matcher(input).find()) {
            hmap.put("message", getStringResource(context, R.string.password_whiteSpaces));
            return hmap;
        } else if (!totalCharacters.matcher(input).find()) {
            hmap.put("message", getStringResource(context, R.string.password_characters));
            return hmap;
        }
        hmap.put("message", "Valid");
        return hmap;
    }

    /**
     * Get the String from the resources
     *
     * @param context  application Context
     * @param resource resource id
     * @return a string
     */
    private String getStringResource(Context context, int resource) {
        return context.getResources().getString(resource);
    }
}
