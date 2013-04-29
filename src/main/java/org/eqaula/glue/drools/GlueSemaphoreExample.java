/*
 * Copyright 2013 jlgranda.
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
package org.eqaula.glue.drools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import org.eqaula.glue.model.management.Method;
import org.eqaula.glue.model.management.Target;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugWorkingMemoryEventListener;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 *
 * @author jlgranda
 */
public class GlueSemaphoreExample {
    public static final void main(final String[] args) {
        final KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();

        // this will parse and compile in one step
        kbuilder.add( ResourceFactory.newClassPathResource( "GlueSemaphore.drl", 
                                                            GlueSemaphoreExample.class ),
                      ResourceType.DRL );

        // Check the builder for errors
        if ( kbuilder.hasErrors() ) {
            System.out.println( kbuilder.getErrors().toString() );
            throw new RuntimeException( "Unable to compile \"GlueSemaphore.drl\"." );
        }

        // get the compiled packages (which are serializable)
        final Collection<KnowledgePackage> pkgs = kbuilder
                .getKnowledgePackages();

        // add the packages to a knowledgebase (deploy the knowledge packages).
        final KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( pkgs );

        final StatefulKnowledgeSession ksession = kbase
                .newStatefulKnowledgeSession();
        ksession.setGlobal( "list",
                            new ArrayList<Object>() );

        ksession.addEventListener( new DebugAgendaEventListener() );
        ksession.addEventListener( new DebugWorkingMemoryEventListener() );

        // setup the audit logging
        // Remove comment to use FileLogger
        // KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger( ksession, "./helloworld" );
        
        // Remove comment to use ThreadedFileLogger so audit view reflects events whilst debugging
        // KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newThreadedFileLogger( ksession, "./helloworld", 1000 );

        final Target target = new Target();
        target.setValue(new BigDecimal(100));
        final Method method = new Method();
        target.addMethod(method);
        
        ksession.insert( method );

        ksession.fireAllRules();

        // Remove comment if using logging
        // logger.close();

        ksession.dispose();
    }
}
