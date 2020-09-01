package com.learning.kotlin

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import java.util.*


class Main : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch

    // Textures
    private lateinit var background: Texture
    private lateinit var pipeAbove: Texture
    private lateinit var pipeBelow: Texture
    private lateinit var birds: Array<Texture>

    // Shapes for collisions
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var birdShape: Circle
    private lateinit var pipeAboveShape: Rectangle
    private lateinit var pipeBelowShape: Rectangle

    // Configuration Attributes
    private var score = 0
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
    private lateinit var scoreTextGlyphLayout: GlyphLayout

    private var random: Random = Random()

    override fun create() {
        initTextures()
        initConfiguration()
        initShapes()
    }

    override fun render() {
        verifyGameState()
        evaluateScore()
        drawTextures()
        detectCollisions()
        drawCollisionShapes()
    }

    override fun dispose() {
    }

    private fun initConfiguration() {
        screenWidth = Gdx.graphics.width.toFloat()
        screenHeight = Gdx.graphics.height.toFloat()

        birdPositionY = screenHeight / 2

        pipePositionX = screenWidth

        score = 0
    }

    private fun initShapes() {
        shapeRenderer = ShapeRenderer()
        birdShape = Circle()
        pipeAboveShape = Rectangle()
        pipeBelowShape = Rectangle()
    }

    private fun initTextures() {
        batch = SpriteBatch()

        scoreText = BitmapFont()
        scoreText.color = Color.WHITE
        scoreText.data.scale(10f)

        scoreTextGlyphLayout = GlyphLayout()

        birds = arrayOf(Texture("passaro1.png"),
                Texture("passaro2.png"),
                Texture("passaro3.png"))

        background = Texture("fundo.png")

        pipeAbove = Texture("cano_topo_maior.png")
        pipeBelow = Texture("cano_baixo_maior.png")
    }

    private fun drawTextures() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        batch.draw(background, 0f, 0f, screenWidth, screenHeight)
        batch.draw(birds[animationIndex.toInt()], birdPositionX, birdPositionY)
        batch.draw(pipeAbove, pipePositionX, screenHeight - pipeAbove.height + spaceBetweenPipes / 2 + pipePositionY)
        batch.draw(pipeBelow, pipePositionX, screenHeight / 2 - pipeBelow.height - spaceBetweenPipes / 2 + pipePositionY)


        scoreTextGlyphLayout.setText(scoreText, "$score")

        scoreText.draw(batch, "$score", screenWidth / 2 - scoreTextGlyphLayout.width / 2, screenHeight - 110)

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

        if (Intersector.overlaps(birdShape, pipeAboveShape) ||
                Intersector.overlaps(birdShape, pipeBelowShape)) {
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
                    gravity = -20f
                    gameState = GameState.RUNNING
                }
            }

            GameState.RUNNING -> {
                if (justTouched) {
                    gravity = -20f
                    gameState = GameState.RUNNING
                }

                pipePositionX -= deltaTime * 200
                if (pipePositionX < -pipeAbove.width) {
                    pipePositionX = screenWidth
                    pipePositionY = (random.nextInt(800) - 400).toFloat()
                    passedThroughPipe = false
                }

                applyGravity()

                calculatePhysics()
            }

            GameState.GAME_OVER -> {
                if (justTouched) {
                    gameState = GameState.WAITING_TO_START
                    initConfiguration()
                }
            }
        }

        animationIndex += deltaTime * 10

        if (animationIndex >= birds.size) {
            animationIndex = 0f
        }
    }

    private fun evaluateScore() {
        if (pipePositionX < 50 - birds.first().width && !passedThroughPipe) {
            passedThroughPipe = true
            score++
        }
    }

    private fun applyGravity() {
        if (birdPositionY > 0 || justTouched) {
            birdPositionY -= gravity
        }
    }

    private fun calculatePhysics() {
        gravity++
    }
}