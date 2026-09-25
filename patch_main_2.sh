#!/bin/bash
awk '
/onAdminLoginClick = { email, password ->/ {
    print "                onGoogleSignInClick = {"
    print "                    performGoogleSignIn(authViewModel)"
    print "                },"
    print "                onAdminLoginClick = { email, password ->"
    next
}
{ print $0 }
' app/src/main/java/com/asistenpetani/MainActivity.kt > temp.kt && mv temp.kt app/src/main/java/com/asistenpetani/MainActivity.kt
