package com.sarhanm.resolver

import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.ModuleVersionSelector
import org.junit.Test

import static org.junit.Assert.fail

/**
 *
 * @author mohammad sarhan
 */
class VersionResolveViaManifestTest {

    @Test
    void testManifestVersion() {
        def file = new File("src/test/resources/versions.yaml")

        def options = getOption(file.toURI().toString())

        def selectorMock = new MockFor(ModuleVersionSelector)
        selectorMock.demand.getVersion(1) { params -> 'auto' }
        selectorMock.demand.getGroup { params -> 'com.coinfling' }
        selectorMock.demand.getName { params -> 'auth-service-api' }

        def detailsMock = new MockFor(DependencyResolveDetails)
        detailsMock.demand.getRequested(1) { params -> selectorMock.proxyInstance() }

        def details = detailsMock.proxyInstance()

        def projectMock = new MockFor(Project)
        projectMock.demand.file{ param -> param.endsWith('src/test/resources/versions.yaml') ? file : null }
        projectMock.ignore.getParent(){}
        projectMock.ignore.getConfigurations(){}

        def resolver = new VersionResolverInternal(projectMock.proxyInstance(), options,null, null, null)
        def ver = resolver.resolveVersionFromManifest(details)
        assert ver == "1.0-SNAPSHOT"

    }

    @Test
    void testManifestVersionMissing() {
        def file = new File("src/test/resources/versions.yaml")

        def options = getOption(file.toURI().toString())

        def selectorMock = new MockFor(ModuleVersionSelector)
        selectorMock.demand.getVersion { params -> 'auto' }
        selectorMock.demand.getGroup { params -> 'com.coinfling' }
        selectorMock.demand.getName { params -> 'not-there' }

        def detailsMock = new MockFor(DependencyResolveDetails)
        detailsMock.demand.getRequested { params -> selectorMock.proxyInstance() }

        def projectMock = new MockFor(Project)
        projectMock.demand.file{ param -> param.endsWith('src/test/resources/versions.yaml') ? file : null }
        projectMock.ignore.getParent(){}
        projectMock.ignore.getConfigurations(){}

        def resolver = new VersionResolverInternal(projectMock.proxyInstance(), options,null,null,null)
        try {
            def ver = resolver.resolveVersionFromManifest(detailsMock.proxyInstance())
        } catch (IllegalStateException ex) {
            //ignore. expected
            return
        }

        fail("Expected an exception to be thrown")
    }

    @Test
    void testNoExecution() {
        def file = new File("src/test/resources/versions.yaml")

        def options = getOption(file.toURI().toString())

        def selectorMock = new MockFor(ModuleVersionSelector)
        selectorMock.demand.getVersion { params -> '1.2.3' }
        selectorMock.demand.getGroup { params -> 'com.coinfling' }
        selectorMock.demand.getName { params -> 'foobar' }

        def detailsMock = new MockFor(DependencyResolveDetails)
        detailsMock.demand.getRequested { params -> selectorMock.proxyInstance() }

        def projectMock = new MockFor(Project)
        projectMock.demand.file{ param -> param.endsWith('src/test/resources/versions.yaml') ? file : null }
        projectMock.ignore.getParent(){}
        projectMock.ignore.getConfigurations(){}

        def resolver = new VersionResolverInternal(projectMock.proxyInstance(), options, null, null, null)
        def ver = resolver.resolveVersionFromManifest(detailsMock.proxyInstance())
        assert ver == "1.2.3"

    }

    private VersionManifestOption getOption(def url, def username = null, def password = null) {
        [url: url, username: username, password: password] as VersionManifestOption
    }
}
