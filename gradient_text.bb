; Code tested

; Gradient Text Library

; There are functions to draw animated or still gradient text
; and set its properties like color, speed and direction.
; There are no functions to retrieve the properties, you
; have to access the global variables directly.

Global GT_SPEED# = 1.0 ; number of cycles per second, must be > 0.0
Global GT_UPDATE_TIME = 1000 / GT_SPEED# ; time taken for one complete cycle
Global GT_PREV_TIME = MilliSecs() ; records the last time the text was at phase 0.0
Global GT_DIR = 0 ; scroll direction - 0 means left to right, 1 means right to left

; rgb values of first color in gradient

Global GT_R1 = 30
Global GT_G1 = 30
Global GT_B1 = 238

; rgb values of second color in gradient

Global GT_R2 = 48
Global GT_G2 = 223
Global GT_B2 = 233

; differences in rgb values of the two colors (used to calculate intermediate colors)

Global GT_RDIFF = GT_R2 - GT_R1
Global GT_GDIFF = GT_G2 - GT_G1
Global GT_BDIFF = GT_B2 - GT_B1


; Draw gradient text (animated)

Function GradientText(x,y,Lit$,cx = False,cy = False)
    Local i,j
    Local Offset
    Local Length
    Local HalfLength#
    Local Time
    Local Elapsed
    Local Phase#,Factor#

    Time = MilliSecs()
    Elapsed = Time - GT_PREV_TIME

    Phase# = (Float(Elapsed Mod GT_UPDATE_TIME) / GT_UPDATE_TIME)
    
    If Elapsed >= GT_UPDATE_TIME
        GT_PREV_TIME = Time - (Elapsed Mod GT_UPDATE_TIME)
    EndIf

    Length = Len(lit$)

    If cx = True
        x = x - StringWidth(Lit$) / 2
    EndIf
    
    If cy = True
        y = y - StringHeight(Lit$) / 2
    EndIf


    HalfLength# = Float(Length) / 2

    For i = 0 To Length - 1
    
        If GT_DIR = 0
            If (i - (Length * Phase#)) <= 0.0
                j = Length +  (i - (Length * Phase#))
            Else
                j = (i - (Length * Phase#))
            EndIf
        Else
            j = (i + (Length * Phase#)) Mod Length
        EndIf

        If Float(j) / Length >= 0.5
            Factor# = 1.0 - (j - HalfLength#) / HalfLength#		
        Else
            Factor# = j / HalfLength#
        EndIf

        Color GT_R1 + Factor# * GT_RDIFF,GT_G1 + Factor# * GT_GDIFF,GT_B1 + Factor# * GT_BDIFF

        Text x + Offset,y,Mid(Lit$,i+1,1),False,False
        Offset = Offset + StringWidth(Mid(Lit$,i+1,1))
    Next
    
    Return 1
End Function


; Old gradient text function, not animated

Function GradientTextClassic(x,y,Lit$,cx = False,cy = False)
    Local i
    Local Offset
    Local Length
    Local HalfLength#
    Local Factor#

    Length = Len(lit$)

    If cx = True
        x = x - StringWidth(Lit$) / 2
    EndIf
    
    If cy = True
        y = y - StringHeight(Lit$) / 2
    EndIf

    HalfLength# = Float(Length) / 2

    For i = 0 To Length - 1
        If Float(i) / Length >= 0.5
            Factor# = 1.0 - (i - HalfLength#) / HalfLength#		
        Else
            Factor# = i / HalfLength#
        EndIf

        Color GT_R1 + Factor# * GT_RDIFF,GT_G1 + Factor# * GT_GDIFF,GT_B1 + Factor# * GT_BDIFF

        Text x + Offset,y,Mid(Lit$,i+1,1),False,False
        Offset = Offset + StringWidth(Mid(Lit$,i+1,1))
    Next
    
    Return 1
End Function

; Set gradient colors. The first three params make up the first color (red, green blue in that order)
; the last three make up the last color.

Function SetGradientTextColor(R1,G1,B1,R2,G2,B2)
    GT_R1 = R1
    GT_G1 = G1
    GT_B1 = B1
    
    GT_R2 = R2
    GT_G2 = G2
    GT_B2 = B2
    
    GT_RDIFF = GT_R2 - GT_R1
    GT_GDIFF = GT_G2 - GT_G1
    GT_BDIFF = GT_B2 - GT_B1
End Function

; Set speed of gradient text (cycles per second, must be > 0.0)

Function SetGradientTextSpeed(Speed#)
    GT_SPEED# = Speed#
    GT_UPDATE_TIME = 1000 / GT_SPEED#
End Function

; Set anim direction - 0 means left to right (default), 1 means right to left

Function SetGradientTextDirection(Dir)
    GT_DIR = (Dir <> 0)
End Function

; Synchronises (resets) gradient text to current time

Function SyncGradientText()
    GT_PREV_TIME = MilliSecs()
End Function