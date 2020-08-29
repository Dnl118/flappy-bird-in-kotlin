package com.learning.kotlin

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class Main : ApplicationAdapter() {

    private lateinit var batch: SpriteBatch
    private lateinit var background: Texture

    private lateinit var birds: Array<Texture>

    private var birdPositionX = 30f
    private var birdPositionY = 0f

    private var screenWidth = 0f
    private var screenHeight = 0f

    private var animationIndex = 0f

    private var gravity = 0f

    private var justTouched = false

    override fun create() {
        batch = SpriteBatch()
        initTextures()
        initConfiguration()
    }

    override fun render() {
        verifyGameState()
        drawTextures()
    }

    override fun dispose() {
    }

    private fun initConfiguration() {
        screenWidth = Gdx.graphics.width.toFloat()
        screenHeight = Gdx.graphics.height.toFloat()

        birdPositionY = screenHeight / 2
    }

    private fun initTextures() {
        birds = arrayOf(Texture("passaro1.png"),
                Texture("passaro2.png"),
                Texture("passaro3.png"))

        background = Texture("fundo.png")
    }

    private fun drawTextures() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        batch.draw(background, 0f, 0f, screenWidth, screenHeight)
        batch.draw(birds[animationIndex.toInt()], birdPositionX, birdPositionY)

        batch.end()
    }

    private fun verifyGameState() {
        justTouched = Gdx.input.justTouched()

        if (justTouched) {
            gravity = -25f
        }

        applyGravity()

        animationIndex += Gdx.app.graphics.deltaTime * 10

        if (animationIndex >= birds.size) {
            animationIndex = 0f
        }

        calculatePhysics()
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