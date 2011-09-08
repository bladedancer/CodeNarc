/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenarc.rule

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ConstructorNode

/**
 * This class tests the @SuppressWarnings functionality.
 *
 * @author Hamlet D'Arcy
 */
class SupressWarningsTest extends AbstractRuleTestCase {

    static boolean failOnClass = false
    static boolean failOnConstructor = false
    static boolean failOnMethod = false
    static boolean failOnProperty = false
    static boolean failOnField = false

    void setUp() {
        super.setUp()
        failOnClass = false
        failOnConstructor = false
        failOnMethod = false
        failOnProperty = false    
        failOnField = false
    }

    void testRuleProperties() {
        assert rule.priority == 2
        assert rule.name == 'ForceViolations'
    }

    @Override
    @SuppressWarnings('JUnitTestMethodWithoutAssert')
    void testThatUnrelatedCodeHasNoViolations() {
        // make sure parent does not run
    }

    void testSuppressOnClass() {
        failOnClass = true

        final SOURCE = '''
            @SuppressWarnings('ForceViolations') class MyClass1 {}
            @SuppressWarnings(["ForceViolations", "SomethingElse"]) class MyClass2 {}
            @SuppressWarnings class MyClass3 {}
            @SuppressWarnings
            class MyClass4 {}
            @SuppressWarnings() class MyClass5 {}
            @SuppressWarnings()
            class MyClass6 {}
            class MyClass7 {}
        '''

        assertViolations SOURCE,
                [lineNumber: 4, sourceLineText: 'class MyClass3 {}'],
                [lineNumber: 6, sourceLineText: 'class MyClass4 {}'],
                [lineNumber: 7, sourceLineText: 'class MyClass5 {}'],
                [lineNumber: 9, sourceLineText: 'class MyClass6 {}'],
                [lineNumber: 10, sourceLineText: 'class MyClass7 {}']
    }

    void testSuppressOnConstructor() {
        failOnConstructor = true
        final SOURCE = '''
            class MyClass1 {
                @SuppressWarnings('ForceViolations')
                MyClass1() {}
            }

            class MyClass2 {
                @SuppressWarnings(["ForceViolations", "SomethingElse"])
                MyClass2() {}
            }

            class MyClass3 {
                @SuppressWarnings MyClass3() {}
            }
            class MyClass4 {
                MyClass4() {}
            }
        '''

        assertTwoViolations SOURCE,
                13, '@SuppressWarnings MyClass3() {}',
                16, 'MyClass4() {}'
    }

    void testVisitProperty() {
        failOnProperty = true
        final SOURCE = '''
            class MyClass1 {
                @SuppressWarnings('ForceViolations')
                def someProperty
            }

            class MyClass2 {
                @SuppressWarnings(["ForceViolations", "SomethingElse"])
                def someProperty
            }

            class MyClass3 {
                @SuppressWarnings def someProperty
            }
            class MyClass4 {
                def someProperty
            }
        '''

        assertTwoViolations SOURCE,
                13, 'def someProperty',
                16, 'def someProperty'
    }

    void testVisitField() {
        failOnField = true
        final SOURCE = '''
            class MyClass1 {
                @SuppressWarnings('ForceViolations')
                def someProperty
            }

            class MyClass2 {
                @SuppressWarnings(["ForceViolations", "SomethingElse"])
                def someProperty
            }

            class MyClass3 {
                @SuppressWarnings def someProperty
            }
            class MyClass4 {
                def someProperty
            }
        '''

        assertTwoViolations SOURCE,
                13, 'def someProperty',
                16, 'def someProperty'
    }

    void testVisitMethod() {
        failOnMethod = true
        final SOURCE = '''
            class MyClass {
                @SuppressWarnings('ForceViolations')
                def method1() {}

                @SuppressWarnings(["ForceViolations", "SomethingElse"])
                def method2() {}

                @SuppressWarnings def method3() {}

                def method4() {}
            }
        '''

        assertTwoViolations SOURCE,
                9, 'def method3()',
                11, 'def method4()'
    }

    protected Rule createRule() {
        new ForceViolationsRule()
    }
}

class ForceViolationsRule extends AbstractAstVisitorRule {
    String name = 'ForceViolations'
    int priority = 2
    Class astVisitorClass = ForceViolationsRuleAstVisitor
}

class ForceViolationsRuleAstVisitor extends AbstractAstVisitor {

    protected void visitClassEx(ClassNode node) {
        if (SupressWarningsTest.failOnClass && isFirstVisit(node)) {
            addViolation node, 'visitClassEx'
        }
    }

    void visitPropertyEx(PropertyNode node) {
        if (SupressWarningsTest.failOnProperty && isFirstVisit(node)) {
            addViolation node, 'visitPropertyEx'
        }
    }

    void visitFieldEx(FieldNode node) {
        if (SupressWarningsTest.failOnField && isFirstVisit(node)) {
            addViolation node, 'visitFieldEx'
        }
    }

    void visitMethodEx(MethodNode node) {
        if (SupressWarningsTest.failOnMethod && isFirstVisit(node)) {
            addViolation node, 'visitMethodEx'
        }
    }

    void visitConstructorEx(ConstructorNode node) {
        if (SupressWarningsTest.failOnConstructor && isFirstVisit(node)) {
            addViolation node, 'visitConstructorEx'
        }
    }

    protected void visitConstructorOrMethodEx(MethodNode node, boolean isConstructor) {
        if (isConstructor && SupressWarningsTest.failOnConstructor && isFirstVisit(node)) {
            addViolation node, 'visitConstructorOrMethodEx'
        }
        if (!isConstructor && SupressWarningsTest.failOnMethod && isFirstVisit(node)) {
            addViolation node, 'visitConstructorOrMethodEx'
        }
    }


}
