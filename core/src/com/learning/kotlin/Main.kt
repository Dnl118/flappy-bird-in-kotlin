package com.learning.kotlin

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.badlogic.gdx.utils.viewport.Viewport
import java.util.Random


class Main : ApplicationAdapter() {

    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport

    private val VIRTUAL_WIDTH = 720f
    private val VIRTUAL_HEIGHT = 1280f

    private lateinit var batch: SpriteBatch

    // Textures
    private lateinit var background: Texture
    private lateinit var pipeAbove: Texture
    private lateinit var pipeBelow: Texture
    private lateinit var gameOver: Texture
    private lateinit var birds: Array<Texture>

    // Shapes for collisions
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var birdShape: Circle
    private lateinit var pipeAboveShape: Rectangle
    private lateinit var pipeBelowShape: Rectangle

    // Configuration Attributes
    private var score = 0
    private var highScore = 0
    private var birdPositionX = 50f
    private var birdPositionY = 0f
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var animationIndex = 0f
    private var gravity = 0f
    private var pipePositionX = 0f
    private var pipePositionY = 0f
    private var spaceBetweenPipes = 240f
    private var passedThroughPipe = false

    private var gameState = GameState.WAITING_TO_START

    private var justTouched = false

    private lateinit var scoreText: BitmapFont
    private lateinit var restartText: BitmapFont
    private lateinit var highScoreText: BitmapFont
    private lateinit var glyphLayout: GlyphLayout

    private lateinit var wingsSound: Sound
    private lateinit var scoreSound: Sound
    private lateinit var collisionSound: Sound

    private var random: Random = Random()

    private val touchToRestart = "Touch to restart!"

    private lateinit var preferences: Preferences

    override fun create() {
        initCamera()
        initTextures()
        initConfiguration()
        initShapes()
        initSounds()
        initScore()
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)

        verifyGameState()
        evaluateScore()
        drawTextures()
        detectCollisions()
//        drawCollisionShapes()
    }

    override fun dispose() {
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    private fun initConfiguration() {
        screenWidth = VIRTUAL_WIDTH
        screenHeight = VIRTUAL_HEIGHT

        birdPositionX = 50f
        birdPositionY = screenHeight / 2

        pipePositionX = screenWidth

        score = 0
    }

    private fun initCamera() {
        camera = OrthographicCamera()
        camera.position.x = VIRTUAL_WIDTH / 2
        camera.position.y = VIRTUAL_HEIGHT / 2
        viewport = StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera)
    }

    private fun initSounds() {
        wingsSound = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"))
        scoreSound = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"))
        collisionSound = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"))
    }

    private fun initShapes() {
        shapeRenderer = ShapeRenderer()
        birdShape = Circle()
        pipeAboveShape = Rectangle()
        pipeBelowShape = Rectangle()
    }

    private fun initScore() {
        preferences = Gdx.app.getPreferences("flappyBird")
        highScore = preferences.getInteger("highScore", 0)
    }

    private fun initTextures() {
        batch = SpriteBatch()

        scoreText = BitmapFont()
        scoreText.color = Color.WHITE
        scoreText.data.scale(10f)

        restartText = BitmapFont()
        restartText.color = Color.GREEN
        restartText.data.scale(1.5f)

        highScoreText = BitmapFont()
        highScoreText.color = Color.RED
        highScoreText.data.scale(1.5f)

        glyphLayout = GlyphLayout()

        birds = arrayOf(Texture("passaro1.png"),
                Texture("passaro2.png"),
                Texture("passaro3.png"))

        background = Texture("fundo.png")

        pipeAbove = Texture("cano_topo.png")
        pipeBelow = Texture("cano_baixo.png")

        gameOver = Texture("game_over.png")
    }

    private fun drawTextures() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.projectionMatrix = camera.combined

        batch.begin()

        batch.draw(background, 0f, 0f, screenWidth, screenHeight)
        batch.draw(birds[animationIndex.toInt()], birdPositionX, birdPositionY)
        batch.draw(pipeAbove, pipePositionX, screenHeight - pipeAbove.height + spaceBetweenPipes / 2 + pipePositionY)
        batch.draw(pipeBelow, pipePositionX, screenHeight / 2 - pipeBelow.height - spaceBetweenPipes / 2 + pipePositionY)

        glyphLayout.setText(scoreText, "$score")
        scoreText.draw(batch, "$score", screenWidth / 2 - glyphLayout.width / 2, screenHeight - 110)

        if (gameState == GameState.GAME_OVER) {
            batch.draw(gameOver, screenWidth / 2 - gameOver.width / 2, screenHeight / 2)

            glyphLayout.setText(restartText, touchToRestart)
            restartText.draw(batch, touchToRestart, screenWidth / 2 - glyphLayout.width / 2, screenHeight / 2 - gameOver.height / 2)

            val highScore = "High score: $highScore"
            glyphLayout.setText(highScoreText, highScore)
            highScoreText.draw(batch, highScore, screenWidth / 2 - glyphLayout.width / 2, screenHeight / 2 - glyphLayout.height * 3.5f)
        }

        batch.end()
    }

    private fun detectCollisions() {
        val birdWidth = birds.first().width
        val birdHeight = birds.first().height
        birdShape.set(birdPositionX + birdWidth / 2f, birdPositionY + birdHeight / 2f, birdWidth / 2f)

        pipeAboveShape.set(pipePositionX,
                screenHeight - pipeAbove.height + spaceBetweenPipes / 2 + pipePositionY,
                pipeAbove.width.toFloat(),
                pipeAbove.height.toFloat())

        pipeBelowShape.set(pipePositionX,
                screenHeight / 2 - pipeBelow.height - spaceBetweenPipes / 2 + pipePositionY,
                pipeBelow.width.toFloat(),
                pipeBelow.height.toFloat())

        if ((Intersector.overlaps(birdShape, pipeAboveShape) ||
                        Intersector.overlaps(birdShape, pipeBelowShape)) &&
                gameState == GameState.RUNNING) {
            collisionSound.play()
            gameState = GameState.GAME_OVER
        }
    }

    private fun drawCollisionShapes() {
        val birdWidth = birds.first().width
        val birdHeight = birds.first().height

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.RED

        shapeRenderer.circle(birdPositionX + birdWidth / 2f, birdPositionY + birdHeight / 2f, birdWidth / 2f)

        shapeRenderer.rect(pipePositionX,
                screenHeight - pipeAbove.height + spaceBetweenPipes / 2 + pipePositionY,
                pipeAbove.width.toFloat(),
                pipeAbove.height.toFloat())

        shapeRenderer.rect(pipePositionX,
                screenHeight / 2 - pipeBelow.height - spaceBetweenPipes / 2 + pipePositionY,
                pipeBelow.width.toFloat(),
                pipeBelow.height.toFloat())

        shapeRenderer.end()
    }

    private fun verifyGameState() {
        val deltaTime = Gdx.app.graphics.deltaTime
        justTouched = Gdx.input.justTouched()

        when (gameState) {
            GameState.WAITING_TO_START -> {
                if (justTouched) {
                    gravity = -15f
                    gameState = GameState.RUNNING
                    wingsSound.play()
                }
            }

            GameState.RUNNING -> {
                if (justTouched) {
                    gravity = -15f
                    gameState = GameState.RUNNING
                    wingsSound.play()
                }

                pipePositionX -= deltaTime * 200
                if (pipePositionX < -pipeAbove.width) {
                    pipePositionX = screenWidth
                    pipePositionY = (random.nextInt(800) - 400).toFloat()
                    passedThroughPipe = false
                }

                val birdHeight = birds.first().height

                if (birdPositionY + birdHeight >= VIRTUAL_HEIGHT) {
                    birdPositionY = VIRTUAL_HEIGHT - birdHeight
                }

                applyGravity()

                calculatePhysics()
            }

            GameState.GAME_OVER -> {
                if (justTouched) {
                    gameState = GameState.WAITING_TO_START
                    initConfiguration()
                }

                if (score > highScore) {
                    highScore = score
                    preferences.putInteger("highScore", highScore)
                    preferences.flush()
                }

                birdPositionX -= Gdx.graphics.deltaTime * 500
            }
        }

        animationIndex += deltaTime * 10

        if (animationIndex >= birds.size) {
            animationIndex = 0f
        }
    }

    private fun evaluateScore() {
        if (pipePositionX < 50 - birds.first().width && !passedThroughPipe) {
            scoreSound.play()
            passedThroughPipe = true
            score++
        }
    }

    private fun applyGravity() {
        if (birdPositionY > 0 || justTouched) {
            birdPositionY -= gravity
        } else {
            birdPositionY = 0f
        }
    }

    private fun calculatePhysics() {
        gravity++
    }
}