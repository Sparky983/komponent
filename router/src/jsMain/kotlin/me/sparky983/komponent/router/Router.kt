package me.sparky983.komponent.router

import kotlinx.browser.window
import me.sparky983.komponent.*

/**
 * A component that provides routing.
 *
 * This component will be replaced by the current page.
 *
 * @param root an optional containing component that does not change between
 * pages.
 * @param routes the routes configuration
 * @since 0.2.0
 */
public fun Html.Router(
    root: (Html.(Children) -> Unit)? = null,
    routes: Routes.() -> Unit
) {
    val configuration = Routes()
    configuration.routes()
    val router = Router()

    fun resolve(path: String): Children? {
        val segments = segments(path).toList()

        for ((patterns, renderer) in configuration.routes) {
            if (segments.size != patterns.size) {
                continue
            }

            val variables = mutableMapOf<String, String>()

            val matches = patterns.zip(segments).all { (pattern, segment) ->
                when (pattern) {
                    is Pattern.Literal -> pattern.value == segment
                    is Pattern.Variable -> {
                        variables[pattern.name] = segment
                        true
                    }
                }
            }

            if (matches) {
                return {
                    renderer(Router.Context(variables))
                }
            }
        }

        val fallback = configuration.fallback ?: return null

        return {
            fallback(Router.Context(mapOf()))
        }
    }

    Provide(router) {
        val callback = { _: Any ->
            router.locations.value = window.location.pathname
        }

        onMount {
            window.addEventListener("popstate", callback)
        }

        onUnmount {
            window.removeEventListener("popstate", callback)
        }

        val page: Children = {
            Dynamic(router.locations) {
                val page = resolve(it) ?: {}
                page()
            }
        }

        if (root == null) {
            page()
        } else {
            root(page)
        }
    }
}

/**
 * A component that links to a client-side route.
 *
 * Note that this cannot be used outside the [Router] component.
 *
 * @param path the client-side route
 * @param className the className prop
 * @param children the children
 * @throws IllegalStateException if the router context could was not provided in this scope
 * @since 0.2.0
 */
public fun Html.Link(
    path: Signal<String>,
    className: Signal<String>? = null,
    children: Children
) {
    val router = context<Router>()

    a(
        href = path,
        onClick = { event ->
            if (!event.defaultPrevented) {
                router.navigate(path.value)
            }
            event.preventDefault()
        },
        className = className,
        children = children
    )
}

/**
 * Represents the router context object.
 *
 * @since 0.2.0
 */
public class Router internal constructor() {
    internal val locations = signal(window.location.pathname)

    /**
     * Navigates to the given path.
     *
     * @param path the path
     * @since 0.2.0
     */
    public fun navigate(path: String) {
        window.history.pushState(js("{}"), /*unused*/ "", path)
        locations.value = path
    }

    /**
     * Provides context for rendering a given route.
     *
     * @throws NoSuchElementException if the path variable does not exist
     * @since 0.2.0
     */
    public class Context internal constructor(private val variables: Map<String, String>) {
        public operator fun get(variable: String): String =
            variables[variable]
                ?: throw NoSuchElementException("No path variable :$variable")
    }
}

private typealias Renderer = Html.(Router.Context) -> Unit

/**
 * The receiver for configuring routes.
 * 
 * @since 0.2.0
 */
public class Routes internal constructor() {
    internal val routes: MutableList<Route> = mutableListOf()
    internal var fallback: Renderer? = null

    /**
     * Defines a new route with the pattern.
     *
     * ```kotlin
     * Router {
     *     Route("/") {
     *         text("Home!")
     *     }
     *     Route("/number/:n") { context ->
     *         text("Your number is ${context["n"]}")
     *     }
     * }
     * ```
     *
     * @param pattern the path pattern
     * @param children a component which renders the content
     * @since 0.2.0
     */
    public fun Routes.Route(pattern: String, children: Renderer) {
        routes.add(Route(pattern(pattern), children))
    }

    /**
     * Provides a fallback route.
     *
     * The most common use-case is to provide a 404 page.
     *
     * @param children a component which renders the fallback page
     * @since 0.2.0
     */
    public fun Routes.Fallback(children: Renderer) {
        fallback = children
    }
}

private fun pattern(pattern: String) =
    segments(pattern)
        .map { segment ->
            if (segment.startsWith(":")) {
                Pattern.Variable(segment.substring(1))
            } else {
                Pattern.Literal(segment)
            }
        }
        .toList()

private fun segments(path: String) =
    path.split("/")
        .asSequence()
        .drop(if (path.startsWith("/")) 1 else 0)

internal data class Route(val patterns: List<Pattern>, val renderer: Renderer)

internal sealed interface Pattern {
    data class Literal(val value: String) : Pattern

    data class Variable(val name: String) : Pattern
}