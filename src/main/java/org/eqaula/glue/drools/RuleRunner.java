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

import java.util.Collection;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.eqaula.glue.model.management.Method;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.api.io.ResourceType;

/**
 *
 * @author jlgranda
 */
public class RuleRunner {

    public RuleRunner() {
    }

    public void runRules(String[] rules,
            Object[] facts) throws Exception {


        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();


        for (int i = 0; i < rules.length; i++) {

            String ruleText = rules[i];

            /*kbuilder.add(ResourceFactory.newClassPathResource(ruleFile,
             RuleRunner.class),
             ResourceType.DRL);*/
            kbuilder.add(ResourceFactory.newInputStreamResource(IOUtils.toInputStream(ruleText)),
                    ResourceType.DRL);

        }


        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        kbase.addKnowledgePackages(pkgs);

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();


        for (int i = 0; i < facts.length; i++) {

            Object fact = facts[i];

            System.out.println("Inserting fact: " + fact);

            ksession.insert(fact);

        }


        ksession.fireAllRules();

    }

    /**
     * Execute rule based on glue BSC Method
     *
     * @param methods
     * @return
     * @throws Exception
     */
    public List<Method> runRules(List<Method> methods) throws Exception {


        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        for (Method method : methods) {

            kbuilder.add(ResourceFactory.newInputStreamResource(IOUtils.toInputStream(method.getContent())),
                    ResourceType.DRL);

        }


        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        kbase.addKnowledgePackages(pkgs);

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();


        for (Method method : methods) {

            System.out.println("Inserting method: " + method);

            ksession.insert(method);

        }


        ksession.fireAllRules();

        ksession.dispose(); // Stateful rule session must always be disposed when finished

        return methods;

    }

    /**
     * Execute rule based on glue BSC Method
     *
     * @param methods
     * @return
     * @throws Exception
     */
    public Method runRules(Method method) throws Exception {

        if (method.getContent() == null || method.getContent().isEmpty()){
            return method;
        }
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(ResourceFactory.newInputStreamResource(IOUtils.toInputStream(method.getContent())),
                ResourceType.DRL);


        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        kbase.addKnowledgePackages(pkgs);

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        ksession.insert(method);

        ksession.fireAllRules();

        ksession.dispose(); // Stateful rule session must always be disposed when finished

        return method;

    }
}
