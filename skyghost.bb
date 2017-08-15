Include "gradient_text.bb"
Include "line_segments.bb"

; graphics settings

Global GRAPHICS_WIDTH
Global GRAPHICS_HEIGHT
Global GRAPHICS_DEPTH
Global GRAPHICS_MODE

; main frame timer

Global MAIN_TIMER = 0

Const MSG_TIME = 120
Global MSG_FRAME = 0

Const GS_FRONT_END = 0
Const GS_GET_READY = 1
Const GS_PLAYING = 2
Const GS_SECTOR_CLEARED = 3
Const GS_GAME_OVER = 4
Const GS_PAUSED = 5
Const GS_QUIT = 6

Global GAME_STATE = GS_FRONT_END

Global SECTOR = 0 ; basically the difficulty level

; player constants/globals

Global CRAFT.Craft
Global PLAYER_SCORE = 0
Global PLAYER_LIVES = 3
Global PLAYER_THRUSTING
Global CRAFT_INVINCIBILITY_TIME = 120 ; number of frames player is invincible for
Global CRAFT_COLLISION = False
Const CRAFT_SIZE = 20
Const CRAFT_ROTATE_INCREMENT# = 8.0
Const CRAFT_MAX_SPEED# = 16.0
Const CRAFT_ACCELERATION# = 1.2

Const CRAFT1 = 0
Const CRAFT2 = 1
Const CRAFT3 = 2

; asteroid constants/globals

Const ASTEROID_MIN_SIZE = 32
Const ASTEROID_MAX_SIZE = 64
Const ASTEROID_MAX_SPEED# = 16.0
Const ASTEROID_MAX_ROTATE_SPEED# = 6.54
Const ASTEROID_MIN_MASS# = 3000.0
Const ASTEROID_FADE_FRAMES = 30

Global ASTEROID_COUNT = 0


; explosion constant

Const EXPLOSION_SIZE = 256


; bullet constants etc

Const BULLET_SIZE = 4
Const BULLET_VEL# = 16.0
Const BULLET_LIFETIME = 2000
Global LAST_BULLET_TIME = 0
Global BULLET_DELAY = 100

; particle constants

Const PARTICLE_MAX_VEL# = 16.0
Const PARTICLE_LIFESPAN = 60



; variables for storing input

Global MOUSE_X# = 0
Global MOUSE_Y# = 0
Global MOUSEHIT1 = 0

Global KEYDOWN_UP = 0
Global KEYDOWN_DOWN = 0
Global KEYDOWN_LEFT = 0
Global KEYDOWN_RIGHT = 0
Global KEYDOWN_CTRL = 0
Global KEYDOWN_SPACE = 0

Global KEYHIT_P = 0
Global KEYHIT_ENTER = 0
Global KEYHIT_ESC = 0

Global CURRENT_TIME = MilliSecs()

; images

Global HEALTH_BAR = 0

; sounds


Global SND_EXPLODE = LoadSound("explode.ogg")
Global SND_CRASH = LoadSound("collide.ogg")
Global SND_SHOOT = LoadSound("shoot.ogg")
Global SND_THRUST = LoadSound("thrust.ogg")
Global CHN_THRUST = 0
Global SND_TELEPORTING_ARRIVAL = LoadSound("teleporting_arrival.ogg")
Global SND_TELEPORTING_DEPARTURE = LoadSound("teleporting_departure.ogg")

; fonts

Global FONT = 0
Global DEBUG_FONT = 0
Global FONT_HEIGHT = 32
Global FONT_WIDTH = 0

; physics constants

Const e# = 1.0; coefficient of restitution

Const ENTITY_CLASS_ASTEROID = 1
Const ENTITY_CLASS_CRAFT = 2

; poly constants/globals

Global POLY_COUNT = 0
;Const MAX_POLYS = 64

Global CRAFT_POLY1 = 0
Global CRAFT_POLY2 = 1
Global CRAFT_POLY3 = 2

Global TRANSFORMATION_POLY1.Polygon2D = CreatePolygon2D(20)
Global TRANSFORMATION_POLY2.Polygon2D = CreatePolygon2D(20)

; types

Type Polygon2D
    Field maxRadius#
    Field numVertices%
    Field firstVertex.Vector2D
End Type

Type Vector2D
    Field x#,y#
End Type

Type Entity2D
    Field class%
    Field position.Vector2D
    Field velocity.Vector2D
    Field angularVelocity#
    Field orientation#,direction#
    Field mass#
    Field polygon.Polygon2D
End Type

Type Craft
    Field entity.Entity2D
    Field ID% ; id number of craft
    Field invincible% ; invincible
    Field invincibleFrames% ; frames craft has been invincible for
    Field shields#
End Type


Type Bullet
    Field x#,y#,a#
    Field DOB ; date of birth
End Type

Type Asteroid
    Field Size
    Field entity.Entity2D
    ;Field alive ; is alive?
    Field fade_frames ; for dead asteroids, number of frames it has been fading for
End Type

Type Explosion
    Field x#,y#
    Field age
End Type



Type Particle
    Field x#,y#
    Field a#
    Field r#,g#,b#
    Field age
    Field v#
End Type

LoadSettings()

Graphics GRAPHICS_WIDTH,GRAPHICS_HEIGHT,GRAPHICS_DEPTH,GRAPHICS_MODE

SeedRnd MilliSecs()

SetupGraphics()

MAIN_TIMER = CreateTimer(30)



SetBuffer BackBuffer()

Repeat
    Select GAME_STATE
        Case GS_FRONT_END
            DoFrontEnd()
        Case GS_QUIT
            End
        Default
            DoGame()
    End Select
Forever



Function Debug()
    Local pc = 0
    Local vc = 0
    Local avc = 0
    Local p.Polygon2D
    Local v.Vector2D
    For p.Polygon2D = Each Polygon2D
        DebugLog "Polygon2D " + pc
        DebugLog ""
        v = p\firstVertex
        For i = 0 To p\numVertices - 1
            DebugLog "Vector2D " + i + " (" + Int(v\x#) + "," + Int(v\y#) + ")"
            ;DebugLog "Vector2D " + i + " (" + v\x# + "," + v\y# + ")"
            v = After v
            vc = vc + 1
        Next
        pc = pc + 1
        avc = avc + p\numVertices
        DebugLog ""
    Next
    DebugLog ""
    DebugLog "Poly count = " + pc
    DebugLog "Vector2D count = " + vc
    DebugLog "Alt Vector2D count = " + avc
End Function


Function SetupGraphics()

    ; load fixed width font hopefully

    FONT = LoadFont("./UbuntuMono-B.ttf",FONT_HEIGHT,True,False,False)
    
    If Not FONT
        Print "Sorry could not load font."
        End
    EndIf
    
    SetFont FONT
    FONT_WIDTH = StringWidth("A")

    DEBUG_FONT = LoadFont("./Ubuntu-R.ttf",25,0,0,0)

    ; create health bar
    
    HEALTH_BAR = LoadImage("health_bar.png")

    ; set text colors
    
    SetGradientTextColor(0,0,255,0,255,255)
End Function

Function LoadSettings()
    GRAPHICS_WIDTH = 1024
    GRAPHICS_HEIGHT = 768
    GRAPHICS_DEPTH = 16
    GRAPHICS_MODE = 1
    Return 0
End Function

Function CreateScene(sector_num = 0)
    Local i,j
    
    ; create player and various enemies etc

    For i = 0 To sector_num
        CreateAsteroid(Rand(0,GRAPHICS_WIDTH),Rand(0,GRAPHICS_HEIGHT),Rand(ASTEROID_MIN_SIZE,ASTEROID_MAX_SIZE),Rnd(0.0,360.0))	
    Next

End Function


Function DestroyScene()
    Local a.Asteroid
    Local b.Bullet
    Local p.Particle
    
    For a = Each Asteroid
        
        DestroyAsteroid(a)
        
    Next

    For b = Each Bullet
        Delete b
    Next
    For p = Each Particle
        Delete p
    Next

End Function



Function DoGame()
    
    Local frames

    CreateScene(0)
    CreatePlayer()
    GAME_STATE = GS_GET_READY
    PlaySound SND_TELEPORTING_ARRIVAL

    Repeat
    
        If GAME_PAUSED = True
            GetInput()
            If KEYHIT_P
                GAME_PAUSED = False
                WaitTimer(MAIN_TIMER)
            EndIf
        Else
    
            Frames = WaitTimer(MAIN_TIMER)
            
            For i = 1 To Frames
                GetInput()
                If KEYHIT_P
                    GAME_PAUSED = True
                    Exit
                EndIf
                If KEYHIT_ESC
                    GAME_STATE = GS_FRONT_END
                    MSG_FRAME = 0
                    SECTOR = 0
                    DestroyPlayer()
                    DestroyScene()
                    Goto FrontEnd
                EndIf
                
                UpdateGame()
                If GAME_STATE = GS_FRONT_END
                    Goto FrontEnd
                End If

            Next
        EndIf

        DrawGame()
    Forever
    
    
    .FrontEnd
    
    Return 1
    
End Function




Function UpdateGame()

    Select GAME_STATE
    
        Case GS_GET_READY
            
            UpdateEntities()
            
            MSG_FRAME = MSG_FRAME + 1
            If MSG_FRAME = MSG_TIME
                MSG_FRAME = 0
                GAME_STATE = GS_PLAYING
            End If


        Case GS_PLAYING
        
            UpdateEntities()
        
            If ASTEROID_COUNT = 0
                GAME_STATE = GS_SECTOR_CLEARED
                PlaySound SND_TELEPORTING_DEPARTURE
                MSG_FRAME = 0
            End If

        Case GS_SECTOR_CLEARED
        
            UpdateEntities()

            MSG_FRAME = MSG_FRAME + 1
            If MSG_FRAME = MSG_TIME
                MSG_FRAME = 0
                GAME_STATE = GS_GET_READY
                PlaySound SND_TELEPORTING_ARRIVAL
                SECTOR = SECTOR + 1
                DestroyScene()	
                CreateScene(SECTOR)
                CRAFT\invincible = True	
                CRAFT\invincibleFrames = 0
                CRAFT\entity\position\x# = 0.5 * GRAPHICS_WIDTH
                CRAFT\entity\position\y# = 0.5 * GRAPHICS_HEIGHT
                CRAFT\entity\direction# = 270.0
                CRAFT\entity\orientation# = 270.0
                CRAFT\entity\velocity\x# = 0.0
                CRAFT\entity\velocity\y# = 0.0
            End If
        
        Case GS_GAME_OVER
        
            UpdateEntities()
            
            MSG_FRAME = MSG_FRAME + 1
            If MSG_FRAME = MSG_TIME
            
                MSG_FRAME = 0
                GAME_STATE = GS_FRONT_END
                SECTOR = 0
                DestroyPlayer()
                DestroyScene()
                
            End If

        
    End Select	

    Return 1
End Function

Function UpdateEntities()

    If Not (GAME_STATE = GS_GAME_OVER) Then UpdatePlayer()	
    UpdateBullets()
    UpdateAsteroids()
    UpdateExplosions()
    UpdateParticles()
    EntityCollisions()
    BulletCollisions()

End Function

Function DrawGame()
    If CRAFT_COLLISION = True
        ClsColor 255,255,255
        CRAFT_COLLISION = False
    Else
        ClsColor 0,0,0
    EndIf

    Cls
        If Not GAME_STATE = GS_GAME_OVER Then DrawPlayer()
        DrawBullets()
        DrawParticles()
        DrawAsteroids()
        ;DrawExplosions()
        DrawDisplay()

    Flip

    Return 1
End Function



Function DoFrontEnd()

    Local frames

    While GAME_STATE = GS_FRONT_END
        frames = WaitTimer(MAIN_TIMER)
        For i = 1 To frames
            GetInput()
            UpdateFrontEnd()
        Next
        DrawFrontEnd()
    Wend

End Function



Function UpdateFrontEnd()
    If KEYHIT_ENTER Then GAME_STATE = GS_PLAYING
    If KEYHIT_ESC Then End
End Function


Function DrawFrontEnd()
    Cls
        GradientText(0.5 * GRAPHICS_WIDTH,0.25 * GRAPHICS_HEIGHT,"     SKYGHOST 1.0     ",True,True)
        GradientText(0.5 * GRAPHICS_WIDTH,0.5 * GRAPHICS_HEIGHT,"     [PRESS ENTER TO PLAY]     ",True,True)
        
        Color 255,255,255
    Flip()
End Function


Function GetInput()

    CURRENT_TIME = MilliSecs()
    
    KEYHIT_P = KeyHit(25)
    KEYHIT_ENTER = KeyHit(28)
    KEYHIT_ESC = KeyHit(1)
    
    KEYDOWN_UP = KeyDown(200)
    KEYDOWN_DOWN = KeyDown(208)
    KEYDOWN_LEFT = KeyDown(203)
    KEYDOWN_RIGHT = KeyDown(205)
    
    KEYDOWN_CTRL = KeyDown(29)
    KEYDOWN_SPACE = KeyDown(57)
    
End Function


Function EntityCollisions()
    Local e1.Entity2D,e2.Entity2D
    Local velocity1.Vector2D,velocity2.Vector2D,velocityTranslated1.Vector2D,velocityTranslated2.Vector2D
    Local angularVelocity1#,angularVelocity2#
    Local position1.Vector2D,position2.Vector2D
    Local collisionL.Vector2D,collisionR.Vector2D
    Local collisionPoint.Vector2D,normal.Vector2D,normalScaled1.Vector2D,normalScaled2.Vector2D,relativeVelocity.Vector2D
    Local displacement1.Vector2D,displacement2.Vector2D,displacementScaled1.Vector2D,displacementScaled2.Vector2D
    Local sum.Vector2D,OrthogonalVector1.Vector2D,OrthogonalVector2.Vector2D
    Local cross1#,cross2#
    Local distance#,penetration#
    Local velocityDotNormal#
    Local modifiedVelocity#
    Local j1#,j2#
    Local normalLength#
    Local angularMomentum1#,angularMomentum2#
    Local denominator#
    Local inertia1#,inertia2#
    
    velocity1 = New Vector2D
    velocity2 = New Vector2D
    velocityTranslated1 = New Vector2D
    velocityTranslated2 = New Vector2D
    position1 = New Vector2D
    position2 = New Vector2D
    collisionL = New Vector2D
    collisionR = New Vector2D
    collisionPoint = New Vector2D
    normal = New Vector2D
    relativeVelocity = New Vector2D
    displacement1 = New Vector2D
    displacement2 = New Vector2D
    displacementScaled1 = New Vector2D
    displacementScaled2 = New Vector2D
    orthogonalVector1 = New Vector2D
    orthogonalVector2 = New Vector2D
    normalScaled1 = New Vector2D
    normalScaled2 = New Vector2D
    sum = New Vector2D
    
    For e1 = Each Entity2D
        
        ;DebugLog "mass =  " + a1\mass#
        RotatePolygon2D(e1\polygon,TRANSFORMATION_POLY1,e1\orientation#)
        

        e2 = After e1
        While e2 <> Null
            RotatePolygon2D(e2\polygon,TRANSFORMATION_POLY2,e2\orientation#)		
            
            If PolygonsCollide(TRANSFORMATION_POLY1,e1\position\x#,e1\position\y#,TRANSFORMATION_POLY2,e2\position\x#,e2\position\y#)
        
                If e1\class = ENTITY_CLASS_CRAFT Or e2\class = ENTITY_CLASS_CRAFT
                
                    CRAFT_COLLISION = True
                    If CRAFT\invincible = False And CRAFT\Shields >= 0 Then CRAFT\Shields = CRAFT\Shields - 0.125
                End If
            
                PlaySound(SND_CRASH)
            
                ; find angle between entities (from the point of view of entity 1)		
            
                angle# = ATan2(e2\position\y# - e1\position\y#,e2\position\x# - e1\position\x#)

                ; move entities apart

                Repeat
                    e1\position\x# = e1\position\x# + 0.5 * Cos(angle# + 180)
                    e1\position\y# = e1\position\y# + 0.5 * Sin(angle# + 180)
                    
                    e2\position\x# = e2\position\x# + 0.5 * Cos(angle#)
                    e2\position\y# = e2\position\y# + 0.5 * Sin(angle#)
                Until Not PolygonsCollide(TRANSFORMATION_POLY1,e1\position\x#,e1\position\y#,TRANSFORMATION_POLY2,e2\position\x#,e2\position\y#)						

                
                velocity1\x# = e1\velocity\x#
                velocity1\y# = e1\velocity\y#
                
                velocity2\x# = e2\velocity\x#
                velocity2\y# = e2\velocity\y#


                inertia1 = e1\polygon\maxRadius# * e1\polygon\maxRadius# * e1\mass# * (2.0 / 5.0) / 3000.0
                inertia2 = e2\polygon\maxRadius# * e2\polygon\maxRadius# * e2\mass# * (2.0 / 5.0) / 3000.0
        
                angularMomentum1 = toRadians(e1\angularVelocity) * inertia1
                angularMomentum2 = toRadians(e2\angularVelocity) * inertia2
                
                normal\x# = e2\position\x# - e1\position\x#
                normal\y# = e2\position\y# - e1\position\y#
                
                position1\x# = e1\position\x#
                position1\y# = e1\position\y#
                
                position2\x# = e2\position\x#
                position2\y# = e2\position\y#
                
                distance = Sqr(normal\x# * normal\x# + normal\y# * normal\y#)

                penetration = e1\polygon\maxRadius# + e2\polygon\maxRadius# - distance
 
                normalLength = Vector2DLength(normal)
        
                If Abs(normalLength) < 0.0001
                    Goto skip
                End If
                
                NormaliseVector2D(normal)

                CopyVector2D(collisionL,position1)
                ScaleVector2D_(normalScaled1,normal,e1\polygon\maxRadius#)
                AddVector2D(collisionL,normalScaled1)
                ScaleVector2D(collisionL,0.5)
                
                CopyVector2D(collisionR,position2)
                ScaleVector2D_(normalScaled1,normal,e2\polygon\maxRadius#)
                SubtractVector2D(collisionR,normalScaled1)
                ScaleVector2D(collisionR,0.5)
                
                CopyVector2D(collisionPoint,CollisionL)
                AddVector2D(collisionPoint,collisionR)
                        
                SubtractVector2D_(displacement1,collisionPoint,position1)
                SubtractVector2D_(displacement2,collisionPoint,position2)
                
                angularVelocity1 = toRadians(e1\angularVelocity)
                angularVelocity2 = toRadians(e2\angularVelocity)

                OrthogonalVector2D_(OrthogonalVector1,displacement1)
                OrthogonalVector2D_(OrthogonalVector2,displacement2)

                ScaleVector2D(OrthogonalVector1,angularVelocity1#)
                ScaleVector2D(OrthogonalVector2,angularVelocity2#)
                
                AddVector2D_(velocityTranslated1,velocity1,OrthogonalVector1)
                AddVector2D_(velocityTranslated2,velocity2,OrthogonalVector2)

                SubtractVector2D_(relativeVelocity,velocityTranslated1,velocityTranslated2)
                        
                velocityDotNormal = DotProduct(relativeVelocity,normal)

                If velocityDotNormal < 0.0
                    Goto skip
                End If
                
                denominator = (1.0 / e1\mass# + 1.0 / e2\mass#) * DotProduct(normal,normal)
                
                cross1# = CrossProduct_(displacement1,normal)
                cross2# = CrossProduct_(displacement2,normal)
                
                cross1# = cross1# * (1.0 / inertia1)
                cross2# = cross2# * (1.0 / inertia2)
                
                ScaleVector2D_(displacementScaled1,displacement1,cross1)
                ScaleVector2D_(displacementScaled2,displacement2,cross2)
                
                AddVector2D_(sum,displacementScaled1,displacementScaled2)

                denominator = denominator + DotProduct(sum,normal)
    
                modifiedVelocity = velocityDotNormal / denominator

                j1 = -(1.0 + 1.0) * modifiedVelocity;
                j2 = -(1.0 + 1.0) * modifiedVelocity;

                ScaleVector2D_(normalScaled1,normal,j1 / e1\mass#)
                AddVector2D(velocity1,normalScaled1)

                ScaleVector2D_(normalScaled2,normal,j2 / e2\mass#)
                SubtractVector2D(velocity2,normalScaled2)
                
                e1\velocity\x# = velocity1\x#
                e1\velocity\y# = velocity1\y#
                
                e2\velocity\x# = velocity2\x#
                e2\velocity\y# = velocity2\y#
                
                e1\direction# = ATan2(velocity1\y#,velocity1\x#)
                e2\direction# = ATan2(velocity2\y#,velocity2\x#)
                
                ScaleVector2D_(normalScaled1,normal,j1)
                cross1# = CrossProduct_(displacement1,normalScaled1)
                angularMomentum1# = angularMomentum1# + cross1#
                angularVelocity1# = angularMomentum1# / inertia1#
                
                ScaleVector2D_(normalScaled2,normal,j2)
                cross2# = CrossProduct_(displacement2,normalScaled2)
                angularMomentum2# = angularMomentum2# + cross2#
                angularVelocity2# = angularMomentum2# / inertia2#
                
                e1\angularVelocity = toDegrees(angularVelocity1#)
                e2\angularVelocity = toDegrees(angularVelocity2#)
                                
            EndIf
            .skip
            e2 = After e2
        Wend

        For ex.Explosion = Each Explosion
            
            dist# = Sqr((e1\position\x# - ex\x#)^2 + (e1\position\y# - ex\y#)^2)
            If dist# <= EXPLOSION_SIZE
                angle# = ATan2(e1\position\y# - ex\y#,e1\position\x# - ex\x#)				
                
                e1\velocity\x# = e1\velocity\x# + (2000 * (1.0 - dist# / EXPLOSION_SIZE) * Cos(angle#)) / e1\mass#
                e1\velocity\y# = e1\velocity\y# + (2000 * (1.0 - dist# / EXPLOSION_SIZE) * Sin(angle#)) / e1\mass#
                
                e1\direction# = AWrap(Atan2(e1\velocity\y#,e1\velocity\x#))
                    
                
            EndIf
            


        Next

    Next

    Delete velocity1
    Delete velocity2
    Delete velocityTranslated1
    Delete velocityTranslated2
    Delete position1
    Delete position2
    delete collisionL
    Delete collisionR
    Delete collisionPoint
    Delete normal
    Delete relativeVelocity
    Delete displacement1
    Delete displacement2
    Delete displacementScaled1
    Delete displacementScaled2
    Delete orthogonalVector1
    Delete orthogonalVector2
    Delete normalScaled1
    Delete normalScaled2
    Delete sum
    
End Function

Function CopyVector2D(dest.Vector2D,source.Vector2D)
    dest\x# = source\x#
    dest\y# = source\y#
End Function

Function ScaleVector2D_(dest.Vector2D,source.Vector2D,factor#)
    dest\x# = source\x# * factor#
    dest\y# = source\y# * factor#
End Function

Function ScaleVector2D(dest.Vector2D,factor#)
    dest\x# = dest\x# * factor#
    dest\y# = dest\y# * factor#
End Function

Function AddVector2D_(dest.Vector2D,left.Vector2D,right.Vector2D)
    dest\x# = left\x# + right\x#
    dest\y# = left\y# + right\y#
End Function

Function AddVector2D(dest.Vector2D,source.Vector2D)
    dest\x# = dest\x# + source\x#
    dest\y# = dest\y# + source\y#
End Function

Function SubtractVector2D_(dest.Vector2D,left.Vector2D,right.Vector2D)
    dest\x# = left\x# - right\x#
    dest\y# = left\y# - right\y#
End Function

Function SubtractVector2D(dest.Vector2D,source.Vector2D)
    dest\x# = dest\x# - source\x#
    dest\y# = dest\y# - source\y#
End Function

Function Vector2DLength#(v.Vector2D)
    Return Sqr(v\x# * v\x# + v\y# * v\y#)
End Function

Function NormaliseVector2D(v.Vector2D)
    Local length# = Vector2DLength(v)
    v\x# = v\x# / length#
    v\y# = v\y# / length#
End Function

Function DotProduct#(left.Vector2D,right.Vector2D)
    Return left\x# * right\x# + left\y# * right\y#
End Function

Function CrossProduct_#(left.Vector2D,right.Vector2D)
    Local temp.Vector2D = New Vector2D
    Local result#
    temp\x# = left\y#
    temp\y# = -left\x#
    result# = DotProduct(temp,right)
    Delete temp
    Return result#
End Function

Function OrthogonalVector2D_(dest.Vector2D,source.Vector2D)
    dest\x# = source\y#
    dest\y# = -source\x#
End Function

Function toRadians#(value#)
    Return (value# / 360.0) * 2.0 * Pi
End Function

Function toDegrees#(value#)
    Return (value# / (2.0 * Pi)) * 360.0
End Function

Function BulletCollisions()
    Local a1.Asteroid
    Local pa#
    Local k
    
    For a1 = Each Asteroid
        RotatePolygon2D(a1\entity\polygon,TRANSFORMATION_POLY1,a1\entity\orientation#)

        For b.Bullet = Each Bullet
            If PointInPolygon2D(TRANSFORMATION_POLY1,b\x# - a1\entity\position\x#,b\y# - a1\entity\position\y#)
            
                CreateExplosion(b\x#,b\y#)
                    For k = 0 To 10
                        pa# = Rnd(0.0,360.0)
                        CreateParticle(b\x# + 20 * Cos(pa#),b\y# + 20 * Sin(pa#),Rnd(0.0,360.0),255,255,255)
                    Next
                    SplitAsteroid(a1,b\x# - a1\entity\position\x#,b\y# - a1\entity\position\y#,b\a#)
                Delete b
                Exit
            EndIf
        Next
    Next


End Function

Function CreateExplosion(x#,y#)
    Local ex.Explosion

    PlaySound(SND_EXPLODE)
    ex = New Explosion
    ex\x# = x#
    ex\y# = y#
    
    Return 0
End Function

Function UpdateExplosions()
    For ex.Explosion= Each Explosion
        ex\age = ex\age + 1
        If ex\age = 10
            Delete ex
        EndIf
    Next
End Function

Function DrawExplosions()
    For ex.Explosion= Each Explosion
        Color 255,0,255
        Oval ex\x# - 0.5 * EXPLOSION_SIZE,ex\y# - 0.5 * EXPLOSION_SIZE,EXPLOSION_SIZE,EXPLOSION_SIZE,0
    Next
End Function


Function CreatePlayer()
    Local v.Vector2D
    CRAFT = New CRAFT
    CRAFT\entity = New Entity2D
    CRAFT\entity\position = New Vector2D
    CRAFT\entity\velocity = New Vector2D
    CRAFT\entity\class = ENTITY_CLASS_CRAFT
    CRAFT\entity\position\x# = 0.5 * GRAPHICS_WIDTH
    CRAFT\entity\position\y# = 0.5 * GRAPHICS_HEIGHT
    CRAFT\entity\direction = 270.0
    CRAFT\entity\orientation# = 270.0
    CRAFT\entity\velocity\x# = 0.0
    CRAFT\entity\polygon = CreatePolygon2D(4)
    
    CRAFT\Shields# = 1.0
    CRAFT\invincible = True
    CRAFT\invincibleFrames = 0
    
    PLAYER_SCORE = 0
    PLAYER_LIVES = 3

    v = CRAFT\entity\polygon\firstVertex
    
    Restore PLAYER_CRAFT_DATA
    For i = 0 To 3
        Read v\x#
        Read v\y#
        v = After v
    Next
    
    CRAFT\entity\mass# = Polygon2DArea(CRAFT\entity\polygon)
    CalcPolygon2DMaxRadius(CRAFT\entity\polygon)
    
End Function

Function UpdatePlayer()
    Local xv#,yv#
    Local k,a#
    Local speed#
    Local a1.Asteroid
    
    ; update lives etc
    
    If CRAFT\shields <= 0.0


        If PLAYER_LIVES >= 1
            PLAYER_LIVES = PLAYER_LIVES - 1
            CRAFT\shields = 1.0
            CreateExplosion(CRAFT\entity\position\x#,CRAFT\entity\position\y#)
            For k = 0 To 60
                a# = Rnd(0.0,360.0)
                CreateParticle(CRAFT\entity\position\x# + 20 * Cos(a#),CRAFT\entity\position\y# + 20 * Sin(a#),a#,255,0,0)
            Next
            GAME_STATE = GS_GET_READY
            MSG_FRAME = 0
            CRAFT\entity\position\x# = 0.5 * GRAPHICS_WIDTH
            CRAFT\entity\position\y# = 0.5 * GRAPHICS_HEIGHT
            CRAFT\entity\direction# = 270.0
            CRAFT\entity\orientation# = 270.0
            CRAFT\entity\velocity\x# = 0.0
            CRAFT\entity\velocity\y# = 0.0
            CRAFT\invincible = True
            CRAFT\invincibleFrames = 0
        Else
            GAME_STATE = GS_GAME_OVER
        EndIf
    EndIf
    
    If CRAFT\invincible = True
        CRAFT\invincibleFrames = CRAFT\invincibleFrames + 1
        If CRAFT\invincibleFrames = CRAFT_INVINCIBILITY_TIME
            CRAFT\invincible = False
            CRAFT\invincibleFrames = 0
        EndIf
    EndIf
    
    ; wrap player
    
    If CRAFT\entity\position\x# >= GRAPHICS_WIDTH Then CRAFT\entity\position\x# = CRAFT\entity\position\x# - GRAPHICS_WIDTH
    If CRAFT\entity\position\y# >= GRAPHICS_HEIGHT Then CRAFT\entity\position\y# = CRAFT\entity\position\y# - GRAPHICS_HEIGHT
    If CRAFT\entity\position\x# < 0.0 Then CRAFT\entity\position\x# = GRAPHICS_WIDTH + CRAFT\entity\position\x#
    If CRAFT\entity\position\y# < 0.0 Then CRAFT\entity\position\y# = GRAPHICS_HEIGHT + CRAFT\entity\position\y#

    If Not KEYDOWN_UP
        If PLAYER_THRUSTING
            PLAYER_THRUSTING = False
            StopChannel(CHN_THRUST)
        End If
    End If

    If KEYDOWN_UP
        CRAFT\entity\velocity\x# = CRAFT\entity\velocity\x# + CRAFT_ACCELERATION# * Cos(CRAFT\entity\orientation#)
        CRAFT\entity\velocity\y# = CRAFT\entity\velocity\y# + CRAFT_ACCELERATION# * Sin(CRAFT\entity\orientation#)
        For k = 0 To 2
            CreateParticle(CRAFT\entity\position\x# - 20 * Cos(CRAFT\entity\orientation#),CRAFT\entity\position\y# - 20 * Sin(CRAFT\entity\orientation#),AWrap(CRAFT\entity\orientation# + 180),Rand(200,255),Rand(64,255),0)
        Next
        If PLAYER_THRUSTING
            If Not ChannelPlaying(CHN_THRUST)
                CHN_THRUST = PlaySound(SND_THRUST)
            End If
        Else
            PLAYER_THRUSTING = True
            CHN_THRUST = PlaySound(SND_THRUST)
        End If
    ElseIf KEYDOWN_DOWN	
        CRAFT\entity\velocity\x# = CRAFT\entity\velocity\x# + CRAFT_ACCELERATION# * Cos(AWrap(CRAFT\entity\orientation#) + 180.0)
        CRAFT\entity\velocity\y# = CRAFT\entity\velocity\y# + CRAFT_ACCELERATION# * Sin(AWrap(CRAFT\entity\orientation#) + 180.0)

    EndIf

    CRAFT\entity\direction# = ATan2(CRAFT\entity\velocity\y#,CRAFT\entity\velocity\x)
    speed# = Sqr(CRAFT\entity\velocity\x# * CRAFT\entity\velocity\x# + CRAFT\entity\velocity\y# * CRAFT\entity\velocity\y#)

    ; limit player speed

    If speed# > CRAFT_MAX_SPEED#
        CRAFT\entity\velocity\x# = CRAFT_MAX_SPEED# * Cos(CRAFT\entity\direction#)
        CRAFT\entity\velocity\y# = CRAFT_MAX_SPEED# * Sin(CRAFT\entity\direction#)
    End If

    ; adjust player angle
    
    If KEYDOWN_LEFT
        CRAFT\entity\orientation# = AWrap(CRAFT\entity\orientation# - CRAFT_ROTATE_INCREMENT#)
    ElseIf KEYDOWN_RIGHT
        CRAFT\entity\orientation# = AWrap(CRAFT\entity\orientation# + CRAFT_ROTATE_INCREMENT#)
    EndIf

    ; move player

    CRAFT\entity\position\x# = CRAFT\entity\position\x# + speed# * Cos(CRAFT\entity\direction#)
    CRAFT\entity\position\y# = CRAFT\entity\position\y# + speed# * Sin(CRAFT\entity\direction#)	

    ; create bullets
    
    If KEYDOWN_CTRL Or KEYDOWN_SPACE
        
        If CURRENT_TIME - LAST_BULLET_TIME >= BULLET_DELAY
            PlaySound(SND_SHOOT)
            CreateBullet(CRAFT\entity\position\x# + 23.5 * Cos(CRAFT\entity\orientation#),CRAFT\entity\position\y# + 23.5 * Sin(CRAFT\entity\orientation#),CRAFT\entity\orientation#)
            LAST_BULLET_TIME = CURRENT_TIME
        EndIf
    EndIf

End Function

Function DrawPlayer()
    ; Draw player
    
    Color 255,0,0

    RotatePolygon2D(CRAFT\entity\polygon,TRANSFORMATION_POLY1,CRAFT\entity\orientation#)	
    DrawPolygon2D(TRANSFORMATION_POLY1,CRAFT\entity\position\x#,CRAFT\entity\position\y#,0.0)
    
    If CRAFT\invincible
        Color 0,Rand(64,255),Rand(64,255)
        Oval CRAFT\entity\position\x# - 30,CRAFT\entity\position\y# - 30,60,60,False
    EndIf
    
End Function

Function DestroyPlayer()
    DestroyPolygon2D(CRAFT\entity\polygon)
    Delete CRAFT\entity\position
    Delete CRAFT\entity\velocity
    Delete CRAFT\entity
    Delete CRAFT
End Function



Function CreateBullet(x#,y#,a#)
    Local b.Bullet
    
    b = New Bullet
    
    b\x# = x#
    b\y# = y#
    b\a# = a#
    b\DOB = CURRENT_TIME
    
End Function

Function UpdateBullets()
    Local b.Bullet
    For b = Each Bullet

        ; wrap bullets
        If b\x# >= GRAPHICS_WIDTH Then b\x# = b\x# - GRAPHICS_WIDTH
        If b\y# >= GRAPHICS_HEIGHT Then b\y# = b\y# - GRAPHICS_HEIGHT
        If b\x# < 0.0 Then b\x# = GRAPHICS_WIDTH + b\x#
        If b\y# < 0.0 Then b\y# = GRAPHICS_HEIGHT + b\y#

        ; move bullets, delete old bullets
            
        b\x# = b\x# + BULLET_VEL# * Cos(b\a#)
        b\y# = b\y# + BULLET_VEL# * Sin(b\a#)


        If CURRENT_TIME - b\DOB >= BULLET_LIFETIME
            Delete b
        EndIf

    Next

End Function

Function DrawBullets()
    Local b.Bullet
    
    For b = Each Bullet
        Color 255,255,0
        Oval b\x# - 0.5 * BULLET_SIZE,b\y# - 0.5 * BULLET_SIZE,BULLET_SIZE,BULLET_SIZE,False
    Next

End Function

Function CreateAsteroid(x#,y#,r#,direction#)
    Local a.Asteroid
    Local i
    Local v.Vector2D
    Local vc ; vertex count
    Local speed#

    a = New Asteroid
    
    a\entity = New Entity2D
    a\entity\position = New Vector2D
    a\entity\velocity = New Vector2D
    
    vc = Rand(6,9)
    
    a\entity\class = ENTITY_CLASS_ASTEROID
    
    a\entity\polygon = CreatePolygon2D(vc)
    
    a\entity\position\x# = x#
    a\entity\position\y# = y#
    
    speed# = Rnd(2.0,5.0)
    a\entity\velocity\x# = speed# * Cos(direction#)
    a\entity\velocity\y# = speed# * Sin(direction#)
    
    a\entity\direction# = direction#
    a\entity\orientation# = 0.0

    a\entity\angularVelocity = Rnd(-ASTEROID_MAX_ROTATE_SPEED#,ASTEROID_MAX_ROTATE_SPEED#)

    v = a\entity\polygon\firstVertex
    For i = 0 To a\entity\polygon\numVertices - 2
        mynum# = Rnd(i * (360.0 / vc),(i+1) * (360.0 / vc))

        v\x# = r# * Cos(mynum#)
        v\y# = r# * Sin(mynum#)
        
        v = After v
    Next
    
    v\x# = a\entity\polygon\firstVertex\x#
    v\y# = a\entity\polygon\firstVertex\y#
    
    a\entity\mass# = Polygon2DArea(a\entity\polygon)
    
    ; set max radius
    CalcPolygon2DMaxRadius(a\entity\polygon)

    ASTEROID_COUNT = ASTEROID_COUNT + 1

    Return 1


End Function


; breaks an asteroid into 2 new asteroids along a specified line (polar co-ords)

Function SplitAsteroid(a.Asteroid,x#,y#,angle#,depth = 2)
    Local i ; iterator
    Local ar1.Asteroid,ar2.Asteroid ; new asteroids
    Local v1.Vector2D,v2.Vector2D ; vertex iterators
    Local c ; counter
    Local xi1#,yi1# ; first intercept
    Local xi2#,yi2# ; second intercept
    Local vi1.Vector2D ; vertex immediately before first intercept
    Local vi2.Vector2D ; vertex immediately before second intercept
    Local voi1 ; numerical offset of first intercept
    Local voi2 ; numerical offset of second intercept

    RotatePolygon2D(a\entity\polygon,TRANSFORMATION_POLY1,a\entity\orientation#)

    v1 = TRANSFORMATION_POLY1\firstVertex

    For i = 0 To TRANSFORMATION_POLY1\numVertices - 2
        
        v2 = After v1

        LineSegmentIntersection(v1\x#,v1\y#,v2\x#,v2\y#,x#,y#,x# + Cos(angle#),y# + Sin(angle#))
        
        If LINE_INTERSECTION_S# >= 0.0 And LINE_INTERSECTION_S# <= 1.0
            c = c + 1

            If c = 1
                ; found the first intercept
                xi1# = LINE_INTERSECTION_X#
                yi1# = LINE_INTERSECTION_Y#
                
                vi1 = v1
                voi1 = i

            ElseIf c = 2
                ; we've found both intercepts, so store the info and exit the loop
                xi2# = LINE_INTERSECTION_X#
                yi2# = LINE_INTERSECTION_Y#
                
                vi2 = v1
                voi2 = i
                Exit
            EndIf
        EndIf
        v1 = v2
    Next
    
    If c = 2
    
        ; create first new asteroid
        
        ar1 = New Asteroid
        ar1\entity = New Entity2D
        ar1\entity\position = New Vector2D
        ar1\entity\velocity = New Vector2D
        ar1\entity\polygon = CreatePolygon2D(voi2-voi1+3)

        ; first vertex is the first intersection

        ar1\entity\polygon\firstVertex\x# = xi1#
        ar1\entity\polygon\firstVertex\y# = yi1#
        
        ; create vertex list for asteroid 1
        
        v1 = After ar1\entity\polygon\firstVertex
        v2 = After vi1
        
        For i = 0 To voi2 - voi1 - 1
            v1\x# = v2\x#
            v1\y# = v2\y#
            v1 = After v1
            v2 = After v2
        Next



        ; last but one vertex is the second intersection
    
        v1\x# = xi2#
        v1\y# = yi2#
        
        ; last vertex is the first intersection
        
        v1 = After v1
        
        v1\x# = xi1#
        v1\y# = yi1#

        ; establish centre of mass for asteroid 1
        
        xt# = 0.0
        yt# = 0.0
        
        v1 = ar1\entity\polygon\firstVertex
        For i = 0 To ar1\entity\polygon\numVertices - 2
            xt# = xt# + v1\x#
            yt# = yt# + v1\y#
            v1 = After v1
        Next
        
        ox# = xt# / ar1\entity\polygon\numVertices
        oy# = yt# / ar1\entity\polygon\numVertices
        
        v1 = ar1\entity\polygon\firstVertex
        For i = 0 To ar1\entity\polygon\numVertices - 1
            v1\x# = v1\x# - ox#
            v1\y# = v1\y# - oy#
            v1 = After v1
        Next
        
        ; establish position for asteroid 1
        
        ar1\entity\position\x# = a\entity\position\x# + ox#
        ar1\entity\position\y# = a\entity\position\y# + oy#


        ; initialise other fields in asteroid 1
    
        
        ar1\entity\velocity\x# = a\entity\velocity\x#
        ar1\entity\velocity\y# = a\entity\velocity\y#
        ar1\entity\direction# = a\entity\direction
        ar1\entity\orientation# = 0.0
        ar1\entity\mass# = Polygon2DArea(ar1\entity\polygon)
        ar1\entity\angularVelocity = Rnd(-ASTEROID_MAX_ROTATE_SPEED#,ASTEROID_MAX_ROTATE_SPEED#)

        ; create second new asteroid
    
        ar2 = New Asteroid
        ar2\entity = New Entity2D
        ar2\entity\position = New Vector2D
        ar2\entity\velocity = New Vector2D
        ar2\entity\polygon = CreatePolygon2D(voi1 + (TRANSFORMATION_POLY1\numVertices - voi2) + 2)
        
        ar2\entity\polygon\firstVertex\x# = xi2#
        ar2\entity\polygon\firstVertex\y# = yi2#
        
        v1 = After ar2\entity\polygon\firstVertex
        v2 = After vi2
        
        For i = 1 To TRANSFORMATION_POLY1\numVertices - voi2 - 1
        
            v1\x# = v2\x#
            v1\y# = v2\y#
            
            v1 = After v1
            v2 = After v2
        Next
        
        v2 = After TRANSFORMATION_POLY1\firstVertex
        
        For i = 1 To voi1
        
            v1\x# = v2\x#
            v1\y# = v2\y#		

            v1 = After v1
            v2 = After v2
        Next

        v1\x# = xi1#
        v1\y# = yi1#
        
        v1 = After v1
        
        v1\x# = xi2#
        v1\y# = yi2#	

    
        ; establish centre of mass for asteroid 2
        
        xt# = 0.0
        yt# = 0.0
        
        v1 = ar2\entity\polygon\firstVertex
        For i = 0 To ar2\entity\polygon\numVertices - 2
            xt# = xt# + v1\x#
            yt# = yt# + v1\y#
            v1 = After v1
        Next
        
        ox# = xt# / ar2\entity\polygon\numVertices
        oy# = yt# / ar2\entity\polygon\numVertices
        
        v1 = ar2\entity\polygon\firstVertex
        For i = 0 To ar2\entity\polygon\numVertices - 1
            v1\x# = v1\x# - ox#
            v1\y# = v1\y# - oy#
            v1 = After v1
        Next
        
        ; establish position for asteroid 2
        
        ar2\entity\position\x# = a\entity\position\x# + ox#
        ar2\entity\position\y# = a\entity\position\y# + oy#

        ; initialise other fields in asteroid 2
    
        ar2\entity\velocity\x# = a\entity\velocity\x#
        ar2\entity\velocity\y# = a\entity\velocity\y#
        ar2\entity\direction# = a\entity\direction#;AWrap(angle# - 90.0)
        ar2\entity\orientation# = 0.0
        
        ar2\entity\mass# = Polygon2DArea(ar2\entity\polygon)
        ar2\entity\angularVelocity = Rnd(-ASTEROID_MAX_ROTATE_SPEED#,ASTEROID_MAX_ROTATE_SPEED#)
        
        ; calculate poly max radii to use in collision detection
        CalcPolygon2DMaxRadius(ar1\entity\polygon)
        CalcPolygon2DMaxRadius(ar2\entity\polygon)
        
        DestroyAsteroid(a)
        
        ASTEROID_COUNT = ASTEROID_COUNT + 2
        
        If ar1\entity\mass# < ASTEROID_MIN_MASS#
            PLAYER_SCORE = PLAYER_SCORE + (5 * (Int(ar1\entity\mass#) / 100))
                        
            DestroyAsteroid(ar1)
        EndIf
        
        If ar2\entity\mass# < ASTEROID_MIN_MASS#
            PLAYER_SCORE = PLAYER_SCORE + (5  * (Int(ar2\entity\mass#) / 100))
            
            DestroyAsteroid(ar2)
        EndIf
    
    Else

    EndIf

End Function

Function UpdateAsteroids()
    Local a1.Asteroid,a2.Asteroid
    Local speed#
    For a1 = Each Asteroid
        
        RotatePolygon2D(a1\entity\polygon,TRANSFORMATION_POLY1,a1\entity\orientation#)		
        
        ; wrap asteroids
        If a1\entity\position\x# >= GRAPHICS_WIDTH Then a1\entity\position\x# = a1\entity\position\x# - GRAPHICS_WIDTH
        If a1\entity\position\y# >= GRAPHICS_HEIGHT Then a1\entity\position\y# = a1\entity\position\y# - GRAPHICS_HEIGHT
        If a1\entity\position\x# < 0.0 Then a1\entity\position\x# = GRAPHICS_WIDTH + a1\entity\position\x#
        If a1\entity\position\y# < 0.0 Then a1\entity\position\y# = GRAPHICS_HEIGHT + a1\entity\position\y#
        
        ; limit asteroid speed
        
        speed# = Sqr(a1\entity\velocity\x# * a1\entity\velocity\x# + a1\entity\velocity\y# * a1\entity\velocity\y#)

        ; limit player speed

        If speed# > ASTEROID_MAX_SPEED#
            a1\entity\velocity\x# = ASTEROID_MAX_SPEED# * Cos(a1\entity\direction#)
            a1\entity\velocity\y# = ASTEROID_MAX_SPEED# * Sin(a1\entity\direction#)
        End If

        ; move asteroids
        
        a1\entity\position\x# = a1\entity\position\x# + a1\entity\velocity\x#
        a1\entity\position\y# = a1\entity\position\y# + a1\entity\velocity\y#
    
        ; rotate asteroids
        
        a1\entity\orientation# = AWrap(a1\entity\orientation# + a1\entity\angularVelocity)
        
        If a1\entity\angularVelocity > ASTEROID_MAX_ROTATE_SPEED Then a1\entity\angularVelocity = ASTEROID_MAX_ROTATE_SPEED
        If a1\entity\angularVelocity < -ASTEROID_MAX_ROTATE_SPEED Then a1\entity\angularVelocity = -ASTEROID_MAX_ROTATE_SPEED

    .cont
    Next


End Function

Function DrawAsteroids()
    Local a.Asteroid
    Local c
    Local cfact# ; color factor for fading (dead) asteroids

    Color 255,255,0

    For a.Asteroid = Each Asteroid

        Color 255,255,255
        RotatePolygon2D(a\entity\polygon,TRANSFORMATION_POLY1,a\entity\orientation#)
        DrawPolygon2D(TRANSFORMATION_POLY1,a\entity\position\x#,a\entity\position\y#,0.0)

        c = c + 1
    Next

    ;Color 255,255,255
    ;Text 0,0,"Total mass = " + tmass#
    ;Text 0,0,"Total momentum = " + Int(tv# * tmass#)
End Function



Function DestroyAsteroid(a.Asteroid)

    DestroyPolygon2D(a\entity\polygon)
    Delete a\entity\position
    Delete a\entity\velocity
    Delete a\entity
    Delete a
    ASTEROID_COUNT = ASTEROID_COUNT - 1
    Return 1

    Return 0
End Function

Function CreateParticle(x#,y#,a#,r#,g#,b#)
    Local p.Particle

    p = New Particle
    p\x# = x#
    p\y# = y#
    p\a# = a#
    p\r# = r#
    p\g# = g#
    p\b# = b#
    p\age = 0
    p\v# = Rnd(0.0,PARTICLE_MAX_VEL#)
    
End Function

Function UpdateParticles()
    Local p.Particle
    
    For p = Each Particle
        If p\age = PARTICLE_LIFESPAN
            Delete p
            Goto continue
        EndIf
        
        p\x# = p\x# + p\v# * Cos(p\a#)
        p\y# = p\y# + p\v# * Sin(p\a#)

        p\age = p\age + 1

        .continue
    Next
End Function

Function DrawParticles()
    Local p.Particle
    Local cfact#
    
    For p = Each Particle
        cfact# = 1.0 - (p\age / Float(PARTICLE_LIFESPAN))
        Color p\r# * cfact#,p\g# * cfact#,p\b * cfact#
        Oval p\x# - 2,p\y# - 2,4,4,1
    Next
    
End Function


Function DrawDisplay()
    Local sx#,sy#,k
    GradientText(0,0,"SKYGHOST 1.0")
    GradientText(GRAPHICS_WIDTH - 12 * FONT_WIDTH,0,"SCORE " + ZeroPad(Str(PLAYER_SCORE),6))
    GradientText(GRAPHICS_WIDTH - 12 * FONT_WIDTH,FONT_HEIGHT,"LIVES " + ZeroPad(Str(PLAYER_LIVES),6))
    GradientText(GRAPHICS_WIDTH - 14 * FONT_WIDTH,FONT_HEIGHT * 2,"SHIELDS " + LSet("",7))
    sx# = GetScaleX()
    sy# = GetScaleY()
    k = 6 * FONT_WIDTH
    SetScale k / 128.0,FONT_HEIGHT / 32.0
    DrawImageRect(HEALTH_BAR,GRAPHICS_WIDTH - 7 * FONT_WIDTH,FONT_HEIGHT * 2,0,0,ImageWidth(HEALTH_BAR) * CRAFT\Shields,ImageHeight(HEALTH_BAR))	
    SetScale sx,sy
    Select GAME_STATE	
        Case GS_GET_READY
            GradientText(0.5 * GRAPHICS_WIDTH,0.5 * GRAPHICS_HEIGHT - 3 * FONT_HEIGHT,"SECTOR " + SECTOR,True,True)
            GradientText(0.5 * GRAPHICS_WIDTH,0.5 * GRAPHICS_HEIGHT - 2 * FONT_HEIGHT,"GET READY!",True,True)		
        Case GS_SECTOR_CLEARED
            GradientText(0.5 * GRAPHICS_WIDTH,0.5 * GRAPHICS_HEIGHT - 3 * FONT_HEIGHT,"SECTOR " + SECTOR + " CLEARED!",True,True)
            GradientText(0.5 * GRAPHICS_WIDTH,0.5 * GRAPHICS_HEIGHT - 2 * FONT_HEIGHT,"TELEPORTING...",True,True)
        Case GS_GAME_OVER
            GradientText(0.5 * GRAPHICS_WIDTH,0.5 * GRAPHICS_HEIGHT - 3 * FONT_HEIGHT,"GAME OVER!",True,True)
            GradientText(0.5 * GRAPHICS_WIDTH,0.5 * GRAPHICS_HEIGHT - 2 * FONT_HEIGHT,"YOU SCORED " + PLAYER_SCORE,True,True)	
    End Select

    Color 255,255,255
    
End Function

; creates uninitialised polygon

Function CreatePolygon2D.Polygon2D(numVertices)
    Local p.Polygon2D
    Local v.Vector2D
    
    p = New Polygon2D
    
    If p <> Null
        v = New Vector2D
        If v = Null
            FatalError()
        EndIf
        p\firstVertex = v
        For i = 1 To numVertices - 1
            v = New Vector2D
            If v = Null
                FatalError()
            EndIf
        Next
    
        p\numVertices = numVertices
        
        POLY_COUNT = POLY_COUNT + 1
        Return p
    Else
        FatalError()
    EndIf

End Function

Function CalcPolygon2DMaxRadius(p.Polygon2D)
    Local v.Vector2D
    Local i
    Local r#
    Local maxRadius#

    v = p\firstVertex	
    For i = 0 To p\numVertices - 1
        r# = Sqr(v\x# * v\x# + v\y# * v\y#)
        If r# > maxRadius#
            maxRadius# = r#
        EndIf
        v = After v
    Next
    
    p\maxRadius# = maxRadius#
    
    Return 1
    
End Function

Function DrawPolygon2D(p.Polygon2D,x#,y#,a#)
    Local i
    Local v1.Vector2D,v2.Vector2D
    
    v1 = p\firstVertex
    For i = 0 To p\numVertices - 2
        v2 = After v1
        Line(x# + v1\x#,y# + v1\y#,x# + v2\x#,y# + v2\y#)
        v1 = v2
        ;Text x# + v1\x#,y# + v1\y#,Str(i),True,True
    Next
    
    ;Line x# + v1\x#,y# + v1\y#,x# + p\firstVertex\x#,y# + p\firstVertex\y#
    
    Return 1

    Return 0
End Function

Function RotatePolygon2D(source.Polygon2D,dest.Polygon2D,a#)
    Local i
    Local v1.Vector2D,v2.Vector2D
    
    dest\numVertices = source\numVertices
    dest\maxRadius# = source\maxRadius#
    
    v1 = source\firstVertex
    v2 = dest\firstVertex
    If v1 = v2
        PRINT "AARGH"
    End If

    For i = 0 To source\numVertices - 1

        v2\x# = v1\x# * Cos(a#) - v1\y# * Sin(a#)
        v2\y# = v1\x# * Sin(a#) + v1\y# * Cos(a#)
        
        v1 = After v1
        v2 = After v2
    Next
    Return 1
    
    Return 0
End Function

Function Polygon2DArea#(p.Polygon2D)
    Local i
    Local x1#,y1#
    Local x2#,y2#
    Local y1r#
    Local x2r#
    Local a#,area#

    Local v1.Vector2D = After p\firstVertex
    Local v2.Vector2D

    For i = 1 To p\numVertices - 2
    
        v2 = After v1
        x1# = v1\x# - p\firstVertex\x#
        y1# = v1\y# - p\firstVertex\y#
        
        x2# = v2\x# - p\firstVertex\x#
        y2# = v2\y# - p\firstVertex\y#		

        a# = -ATan2(y2#,x2#)
        
        y1r# = x1# * Sin(a#) + y1# * Cos(a#)
        
        x2r# = x2# * Cos(a#) - y2# * Sin(a#)
        
        area# = area# + 0.5 * Abs(x2r#) * Abs(y1r#)			
        
        v1 = After v1

    Next
    
    Return area#

    
End Function

Function RectsOverlap(x1,y1,w1,h1,x2,y2,w2,h2)

End Function

Function PolygonsCollide(p1.Polygon2D,x1#,y1#,p2.Polygon2D,x2#,y2#)
    
    Local v.Vector2D

    ;If RectsOverlap(x1# - p1\maxRadius#,y1# - p1\maxRadius#,2 * p1\maxRadius#,2 * p1\maxRadius#,x2# - p2\maxRadius#,y2# - p2\maxRadius#,2 * p2\maxRadius#,2 * p2\maxRadius#)
        v = p1\firstVertex
    
        For i = 0 To p1\numVertices - 1
            
            If PointInPolygon2D(p2,x1# + v\x# - x2#,y1# + v\y# - y2#) Then Return True
    
            v = After v
        Next
    
        v = p2\firstVertex
    
        For i = 0 To p2\numVertices - 1
            Color 0,255,0
    
            If PointInPolygon2D(p1,x2# + v\x# - x1#,y2# + v\y# - y1#) Then Return True
    
            v = After v
        Next
    ;EndIf
    
    Return False

End Function


Function PointInPolygon2D(p.Polygon2D,x#,y#)

    Local v1.Vector2D
    Local v2.Vector2D
    Local i
    Local c = 0


    v1 = p\firstVertex
    For i = 0 To p\numVertices - 2

        v2 = After v1

        LineSegmentIntersection(x#,y#,x#+1000,y#,v1\x#,v1\y#,v2\x#,v2\y#)
        
        If (v1\x# < v2\x# And LINE_INTERSECTION_T# > 0.0 And LINE_INTERSECTION_T# <= 1.0) Or (v1\x# > v2\x# And LINE_INTERSECTION_T# >= 0.0 And LINE_INTERSECTION_T# < 1.0)
            If LINE_INTERSECTION_S# > 0.0
                c = c + 1
            EndIf
        EndIf

        v1 = After v1

    Next

    Return c = 1
End Function



Function DestroyPolygon2D(p.Polygon2D)
    Local i
    Local v1.Vector2D = p\firstVertex
    Local v2.Vector2D
    
    For i = 0 To p\numVertices - 1
        v2 = After v1
        Delete v1
        v1 = v2
    Next
    Delete p
    POLY_COUNT = POLY_COUNT - 1
    Return 1
End Function

Function ATan3(x1#,y1#,x2#,y2#)
    Local a1#,a2#,r#
    
    a1# = AWrap(ATan2(y1#,x1#))
    a2# = AWrap(ATan2(y2#,x2#))
    
    r# = AWrap(a1# - a2#)
    
    Return r#
    
End Function

Function ZeroPad$(MyString$,amount)
    While Len(MyString$) < amount
        MyString = "0" + MyString
    Wend
    Return MySTring
End Function

Function FatalError()
End
End Function


Function AWrap#(a#)
    If a# >= 360.0
        a# = a# Mod 360.0
    ElseIf a# < 0.0
        a# = 360.0 + a#
    EndIf
    Return a#
End Function



.PLAYER_CRAFT_DATA
Data 23.547258,0.000000
Data -11.77363,-12.85575
Data -11.77363,12.855752
Data 23.547258,0.000000
