Global LINE_INTERSECTION_S# = 0.0
Global LINE_INTERSECTION_T# = 0.0

Global LINE_INTERSECTION_X# = 0.0
Global LINE_INTERSECTION_Y# = 0.0

; this function calculates if two line segments intersect
; if they do, it stores the intersection point in the global variables
; LINE_INTERSECTION_X#,LINE_INTERSECTION_Y#
; and stores the intersection points represented as fractions of the line segments in
; LINE_INTERSECTION_S#,LINE_INTERSECTION_T#

; tested

Function LineSegmentIntersection(s1x1#,s1y1#,s1x2#,s1y2#,s2x1#,s2y1#,s2x2#,s2y2#)
    Local e1x#,e1y#,e2x#,e2y# ; end points
    Local sub_s#,sub_i#
    Local cs#,ci#
    

    ; calculate end points
    
    e1x# = s1x2# - s1x1#
    e1y# = s1y2# - s1y1#
    
    e2x# = s2x2# - s2x1#
    e2y# = s2y2# - s2y1#

    ; adjust to prevent division by zero
    If e2x# = 0.0 Then e2x# = 0.0001
    If e2y# = 0.0 Then e2y# = 0.0001


    ; the equations we are solving

    ; (1) s1x1 + s * e1x = s2x1 + t * e2x
    ; (2) s1y1 + s * e1y = s2y1 + t * e2y

    ; find t in terms of s

    sub_i# = s1x1# / e2x# - s2x1# / e2x#
    sub_s# = e1x# / e2x#

    ; collect like terms

    ci# = s2y1# - s1y1# + sub_i# * e2y#
    cs# = e1y# - sub_s# * e2y# 

    ; adjust to prevent division by zero
    If cs# = 0.0 Then cs# = 0.0001

    ; caculate s, the point along segment 1 where the lines meet
    LINE_INTERSECTION_S# = ci# / cs#

    ; caculate t, the point along segment 2 where the lines meet
    LINE_INTERSECTION_T# = (s1y1# - s2y1# + LINE_INTERSECTION_S# * e1y#) / e2y#

    LINE_INTERSECTION_X# = s1x1# + LINE_INTERSECTION_S# * e1x#
    LINE_INTERSECTION_Y# = s1y1# + LINE_INTERSECTION_S# * e1y#	


    If LINE_INTERSECTION_T# >= 0.0 And LINE_INTERSECTION_T# <= 1.0 And LINE_INTERSECTION_S# >= 0.0 And LINE_INTERSECTION_S# <= 1.0
        Return True
    Else
        Return False
    EndIf
    
End Function


















