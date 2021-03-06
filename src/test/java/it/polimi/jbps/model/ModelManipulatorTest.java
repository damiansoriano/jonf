package it.polimi.jbps.model;

import static com.google.common.collect.Lists.newLinkedList;
import static it.polimi.jbps.utils.OntologyUtils.getOntologyFromFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.polimi.jbps.PropertyType;
import it.polimi.jbps.actions.Action;
import it.polimi.jbps.actions.ActionType;
import it.polimi.jbps.actions.PropertyAssignment;
import it.polimi.jbps.bpmn.simulation.Simulator;
import it.polimi.jbps.entities.Context;
import it.polimi.jbps.entities.JBPSIndividual;
import it.polimi.jbps.entities.SimulationState;
import it.polimi.jbps.exception.InvalidPropertyAssignment;
import it.polimi.jbps.form.Form;
import it.polimi.jbps.form.FormsConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class ModelManipulatorTest {
	
	private final static String bpmnOntologyPath = "./src/test/resources/it/polimi/bpmn/simulation/SimplePurchaseRequestBPMN.owl";
	private final static String modelOntologyPath = "./src/test/resources/it/polimi/bpmn/simulation/SimplePurchaseRequestModel.owl";
	private final static String inputDataExample = "./src/test/resources/it/polimi/bpmn/simulation/inputDataExample.json";
	private final static String inputDataExampleWithVariables = "./src/test/resources/it/polimi/bpmn/simulation/inputDataExampleWithVariables.json";
	private final static String testingErrorPath = "./src/test/resources/it/polimi/bpmn/simulation/testingError.owl";

	private final static String modelOntologyWithLiteralDatatypesPath = "./src/test/resources/it/polimi/bpmn/simulation/SimplePurchaseRequestModelWithLiteralDatatypes.owl";
	private final static String inputDataExampleWithVariablesAndLiteralDatatypes = "./src/test/resources/it/polimi/bpmn/simulation/inputDataExampleWithVariablesAndLiteralDatatypes.json";
	
	private final static String createPurchaseOrderURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequest.owl#createPurchaseOrder";
	private final static String changePurchaseOrderURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequest.owl#changePurchaseOrder";
	
	private final static String purchaseRequestClassURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#PurchaseRequest";
	
	private final static String purchaseRequestClientURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#purchaseRequestClient";
	private final static String purchaseRequestResponsibleURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#purchaseRequestResponsible";
	private final static String commentURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#comment";
	private final static String createdDatetimeURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#createdDatetime";
	
	private final static String purchaseRequest01URI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#purchaseRequest01";
		
	private final static String damianURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#damian";
	private final static String employeeURI = "http://www.semanticweb.org/ontologies/2013/5/PurchaseRequestModel.owl#employee";
	
	protected abstract ModelManipulator getModelManipulator(OntModel ontologyModel, Form form);
	protected abstract Simulator getSimulator(OntModel bpmnOntologyModel);
	
	@Test
	public void getActionsReturnsOneAction() throws IOException {
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(modelOntologyPath);
		
		Form form = new Form(FormsConfiguration.createFromFile(inputDataExample, modelOntology));
		
		ModelManipulator manipulator = getModelManipulator(modelOntology, form);
		Simulator simulator = getSimulator(bpmnOntology);
		
		SimulationState createPurchaseOrder = simulator.getStateFromURI(createPurchaseOrderURI);
		
		List<Action> actions = manipulator.getActions(createPurchaseOrder);
		
		assertEquals(1, actions.size());
		
		Action action = actions.get(0);
		assertEquals(ActionType.INSERT, action.getActionType());
		assertEquals(purchaseRequestClassURI, action.getClassURI());
		assertEquals("PurchaseRequest", action.getJbpsClass().toString());
		assertEquals(purchaseRequest01URI, action.getIndividualURI());
		
		List<PropertyAssignment> propertyAssignments = action.getPropertyAssignments();
		assertEquals(2, propertyAssignments.size());
		
		
		PropertyAssignment purchaseRequestClient = null;
		PropertyAssignment purchaseRequestResponsible = null;
		for (PropertyAssignment propertyAssignment : propertyAssignments) {
			
			assertEquals(PropertyType.OBJECT_PROPERTY, propertyAssignment.getPropertyType());
			
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestClientURI)) {
				purchaseRequestClient = propertyAssignment;
			} else if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				purchaseRequestResponsible = propertyAssignment;
			}
		}
		assertNotNull(purchaseRequestClient);
		assertNotNull(purchaseRequestResponsible);
	}
	
	@Test
	public void getPossibleAssignments() throws IOException, InvalidPropertyAssignment {
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(modelOntologyPath);
		Context context = new Context();
		
		Individual prePurchaseRequest01 = modelOntology.getIndividual(purchaseRequest01URI);
		assertNull(prePurchaseRequest01);
		
		Form form = new Form(FormsConfiguration.createFromFile(inputDataExample, modelOntology));
		
		ModelManipulator manipulator = getModelManipulator(modelOntology, form);
		Simulator simulator = getSimulator(bpmnOntology);
		
		SimulationState createPurchaseOrder = simulator.getStateFromURI(createPurchaseOrderURI);
		
		List<Action> actions = manipulator.getActions(createPurchaseOrder);
		Action action = actions.get(0);
		List<PropertyAssignment> propertyAssignments = action.getPropertyAssignments();
		
		
		for (PropertyAssignment propertyAssignment : propertyAssignments) {
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestClientURI)) {
				List<Individual> possibleAssignments = manipulator.getPossibleAssignments(propertyAssignment);
				assertEquals(3, possibleAssignments.size());
				
				Individual damianIndividual = null;
				Individual employeeIndividual = null;
				
				for (Individual individual : possibleAssignments) {
					if (damianURI.equals(individual.getURI())) {
						damianIndividual = individual;
					} else if (employeeURI.equals(individual.getURI())) {
						employeeIndividual = individual;
					}
				}
				
				assertNotNull(damianIndividual);
				assertNotNull(employeeIndividual);
				
				propertyAssignment.setPropertyValue(damianIndividual.getURI());
				
			} else if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				List<Individual> possibleAssignments = manipulator.getPossibleAssignments(propertyAssignment);
				assertEquals(3, possibleAssignments.size());
				
				Individual damianIndividual = null;
				Individual employeeIndividual = null;
				
				for (Individual individual : possibleAssignments) {
					if (damianURI.equals(individual.getURI())) {
						damianIndividual = individual;
					} else if (employeeURI.equals(individual.getURI())) {
						employeeIndividual = individual;
					}
				}
				
				assertNotNull(damianIndividual);
				assertNotNull(employeeIndividual);
				
				propertyAssignment.setPropertyValue(employeeIndividual.getURI());
			}
		}
		
		List<Action> toExecuteActions = newLinkedList();
		toExecuteActions.add(action);
		manipulator.execute(toExecuteActions, context);
		
		Individual purchaseRequest01 = modelOntology.getIndividual(purchaseRequest01URI);
		Property purchaseRequestClientProperty = modelOntology.getProperty(purchaseRequestClientURI);
		RDFNode damianRDFNode = purchaseRequest01.getPropertyValue(purchaseRequestClientProperty);
		
		assertNotNull(damianRDFNode);
		assertTrue(damianRDFNode.isResource());
		assertEquals(damianRDFNode.asResource().getURI(), damianURI);
		
		Property purchaseRequestResponsibleProperty = modelOntology.getProperty(purchaseRequestResponsibleURI);
		RDFNode employeeRDFNode = purchaseRequest01.getPropertyValue(purchaseRequestResponsibleProperty);
		
		assertNotNull(employeeRDFNode);
		assertTrue(employeeRDFNode.isResource());
		assertEquals(employeeRDFNode.asResource().getURI(), employeeURI);
	}
	
	@Test
	public void executeAction() throws IOException, InvalidPropertyAssignment {
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(modelOntologyPath);
		Context context = new Context();
		
		Form form = new Form(FormsConfiguration.createFromFile(inputDataExample, modelOntology));
		
		ModelManipulator manipulator = getModelManipulator(modelOntology, form);
		Simulator simulator = getSimulator(bpmnOntology);
		
		SimulationState createPurchaseOrder = simulator.getStateFromURI(createPurchaseOrderURI);
		
		List<Action> actions = manipulator.getActions(createPurchaseOrder);
		Action action = actions.get(0);
		List<PropertyAssignment> propertyAssignments = action.getPropertyAssignments();
		
		PropertyAssignment purchaseRequestClient = null;
		PropertyAssignment purchaseRequestResponsible = null;
		
		for (PropertyAssignment propertyAssignment : propertyAssignments) {
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestClientURI)) {
				purchaseRequestClient = propertyAssignment;
			} else if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				purchaseRequestResponsible = propertyAssignment;
			}
		}
		
		assertNotNull(purchaseRequestClient);
		assertNotNull(purchaseRequestResponsible);
		
		purchaseRequestClient.setPropertyValue(damianURI);
		purchaseRequestResponsible.setPropertyValue(employeeURI);
		
		List<Action> actionsToExecute = newLinkedList();
		actionsToExecute.add(action);
		manipulator.execute(actionsToExecute, context);
	}
	
	@Test(expected=InvalidPropertyAssignment.class)
	public void executeActionThatViolatesRestrictionThrowException() throws IOException, InvalidPropertyAssignment {
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(modelOntologyPath);
		Context context = new Context();
		
		Form form = new Form(FormsConfiguration.createFromFile(inputDataExample, modelOntology));
		
		ModelManipulator manipulator = getModelManipulator(modelOntology, form);
		Simulator simulator = getSimulator(bpmnOntology);
		
		SimulationState createPurchaseOrder = simulator.getStateFromURI(createPurchaseOrderURI);
		
		List<Action> actions = manipulator.getActions(createPurchaseOrder);
		Action action = actions.get(0);
		List<PropertyAssignment> propertyAssignments = action.getPropertyAssignments();
		
		PropertyAssignment purchaseRequestClient = null;
		PropertyAssignment purchaseRequestResponsible = null;
		
		for (PropertyAssignment propertyAssignment : propertyAssignments) {
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestClientURI)) {
				purchaseRequestClient = propertyAssignment;
			} else if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				purchaseRequestResponsible = propertyAssignment;
			}
		}
		
		assertNotNull(purchaseRequestClient);
		assertNotNull(purchaseRequestResponsible);
		
		purchaseRequestClient.setPropertyValue(damianURI);
		purchaseRequestResponsible.setPropertyValue(damianURI);
		
		List<Action> actionsToExecute = newLinkedList();
		actionsToExecute.add(action);
		manipulator.execute(actionsToExecute, context);
	}
	
	@Test
	public void executeActionThatViolatesRestrictionRollBack() throws IOException {
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(modelOntologyPath);
		Context context = new Context();
		
		Form form = new Form(FormsConfiguration.createFromFile(inputDataExample, modelOntology));
		
		ModelManipulator manipulator = getModelManipulator(modelOntology, form);
		Simulator simulator = getSimulator(bpmnOntology);
		ModelFacade modelFacade = new OntologyModelFacade(modelOntology);
		
		List<JBPSIndividual> allIndividuals = modelFacade.getAllIndividuals();
		assertEquals(3, allIndividuals.size());
		
		SimulationState createPurchaseOrder = simulator.getStateFromURI(createPurchaseOrderURI);
		
		List<Action> actions = manipulator.getActions(createPurchaseOrder);
		Action action = actions.get(0);
		List<PropertyAssignment> propertyAssignments = action.getPropertyAssignments();
		
		PropertyAssignment purchaseRequestClient = null;
		PropertyAssignment purchaseRequestResponsible = null;
		
		for (PropertyAssignment propertyAssignment : propertyAssignments) {
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestClientURI)) {
				purchaseRequestClient = propertyAssignment;
			} else if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				purchaseRequestResponsible = propertyAssignment;
			}
		}
		
		assertNotNull(purchaseRequestClient);
		assertNotNull(purchaseRequestResponsible);
		
		purchaseRequestClient.setPropertyValue(damianURI);
		purchaseRequestResponsible.setPropertyValue(damianURI);
		
		List<Action> actionsToExecute = newLinkedList();
		actionsToExecute.add(action);
		try {
			manipulator.execute(actionsToExecute, context);
			assertTrue(false);
		} catch (InvalidPropertyAssignment ex) { }
		
		allIndividuals = modelFacade.getAllIndividuals();
		assertEquals(3, allIndividuals.size());
	}
	
	@Test
	public void inputDataExampleWithVariables() throws IOException, InvalidPropertyAssignment {
		String variableName = "purchaseOrder";
		
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(modelOntologyPath);
		Context context = new Context();
		
		Form form = new Form(FormsConfiguration.createFromFile(inputDataExampleWithVariables, modelOntology));
		
		ModelManipulator manipulator = getModelManipulator(modelOntology, form);
		Simulator simulator = getSimulator(bpmnOntology);
		
		SimulationState createPurchaseOrder = simulator.getStateFromURI(createPurchaseOrderURI);
		SimulationState changePurchaseOrder = simulator.getStateFromURI(changePurchaseOrderURI);
		
		List<Action> actions = manipulator.getActions(createPurchaseOrder);
		Action action = actions.get(0);
		List<PropertyAssignment> propertyAssignments = action.getPropertyAssignments();
		
		PropertyAssignment purchaseRequestClient = null;
		PropertyAssignment purchaseRequestResponsible = null;
		
		for (PropertyAssignment propertyAssignment : propertyAssignments) {
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestClientURI)) {
				purchaseRequestClient = propertyAssignment;
			} else if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				purchaseRequestResponsible = propertyAssignment;
			}
		}
		
		assertNotNull(purchaseRequestClient);
		assertNotNull(purchaseRequestResponsible);
		
		purchaseRequestClient.setPropertyValue(damianURI);
		
		manipulator.execute(Arrays.asList(action), context);
		
		assertTrue(context.getVariables().containsKey(variableName));
		Individual individual = modelOntology.getIndividual(context.getVariables().get(variableName));
		assertNotNull(individual);
		RDFNode requestClient = individual.getPropertyValue(purchaseRequestClient.getJbpsProperty().getOntProperty());
		assertNotNull(requestClient);
		RDFNode requestResponsible = individual.getPropertyValue(purchaseRequestResponsible.getJbpsProperty().getOntProperty());
		assertNull(requestResponsible);
		
		
		List<Action> changePurchaseOrderActions = manipulator.getActions(changePurchaseOrder);
		Action changePurchaseOrderAction = changePurchaseOrderActions.get(0);
		List<PropertyAssignment> changePurchaseOrderPropertyAssignments = changePurchaseOrderAction.getPropertyAssignments();
		
		PropertyAssignment changePurchaseOrderPurchaseRequestResponsible = null;
		
		for (PropertyAssignment propertyAssignment : changePurchaseOrderPropertyAssignments) {
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				changePurchaseOrderPurchaseRequestResponsible = propertyAssignment;
			}
		}
		assertNotNull(changePurchaseOrderPurchaseRequestResponsible);
		changePurchaseOrderPurchaseRequestResponsible.setPropertyValue(employeeURI);
		
		manipulator.execute(Arrays.asList(changePurchaseOrderAction), context);
		
		assertTrue(context.getVariables().containsKey(variableName));
		Individual puchaseOrder2 = modelOntology.getIndividual(context.getVariables().get(variableName));
		assertNotNull(puchaseOrder2);
		RDFNode requestClient2 = puchaseOrder2.getPropertyValue(purchaseRequestClient.getJbpsProperty().getOntProperty());
		assertNull(requestClient2);
		RDFNode requestResponsible2 = puchaseOrder2.getPropertyValue(purchaseRequestResponsible.getJbpsProperty().getOntProperty());
		assertNotNull(requestResponsible2);
	}
	
	@Test
	public void getPossibleAssignmentsWithLiteralDatatypes() throws IOException, InvalidPropertyAssignment {
		String variableName = "purchaseOrder";
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(modelOntologyWithLiteralDatatypesPath);
		Context context = new Context();
		
		Individual prePurchaseRequest01 = modelOntology.getIndividual(purchaseRequest01URI);
		assertNull(prePurchaseRequest01);
		
		Form form = new Form(FormsConfiguration.createFromFile(inputDataExampleWithVariablesAndLiteralDatatypes, modelOntology));
		
		ModelManipulator manipulator = getModelManipulator(modelOntology, form);
		Simulator simulator = getSimulator(bpmnOntology);
		
		SimulationState createPurchaseOrder = simulator.getStateFromURI(createPurchaseOrderURI);
		
		List<Action> actions = manipulator.getActions(createPurchaseOrder);
		Action action = actions.get(0);
		List<PropertyAssignment> propertyAssignments = action.getPropertyAssignments();
		
		assertEquals(3, propertyAssignments.size());
		
		for (PropertyAssignment propertyAssignment : propertyAssignments) {
			if (propertyAssignment.getPropertyURI().equals(purchaseRequestClientURI)) {
				List<Individual> possibleAssignments = manipulator.getPossibleAssignments(propertyAssignment);
				assertEquals(3, possibleAssignments.size());
				
				Individual damianIndividual = null;
				Individual employeeIndividual = null;
				
				for (Individual individual : possibleAssignments) {
					if (damianURI.equals(individual.getURI())) {
						damianIndividual = individual;
					} else if (employeeURI.equals(individual.getURI())) {
						employeeIndividual = individual;
					}
				}
				
				assertNotNull(damianIndividual);
				assertNotNull(employeeIndividual);
				
				propertyAssignment.setPropertyValue(damianIndividual.getURI());
				
			} else if (propertyAssignment.getPropertyURI().equals(purchaseRequestResponsibleURI)) {
				List<Individual> possibleAssignments = manipulator.getPossibleAssignments(propertyAssignment);
				assertEquals(3, possibleAssignments.size());
				
				Individual damianIndividual = null;
				Individual employeeIndividual = null;
				
				for (Individual individual : possibleAssignments) {
					if (damianURI.equals(individual.getURI())) {
						damianIndividual = individual;
					} else if (employeeURI.equals(individual.getURI())) {
						employeeIndividual = individual;
					}
				}
				
				assertNotNull(damianIndividual);
				assertNotNull(employeeIndividual);
				
				propertyAssignment.setPropertyValue(employeeIndividual.getURI());
			} else if (propertyAssignment.getPropertyURI().equals(commentURI)) {
				List<Individual> possibleAssignments = manipulator.getPossibleAssignments(propertyAssignment);
				assertEquals(0, possibleAssignments.size());
				
//				propertyAssignment.setPropertyValue("Comment of user");
			} else if (propertyAssignment.getPropertyURI().equals(createdDatetimeURI)) {
				List<Individual> possibleAssignments = manipulator.getPossibleAssignments(propertyAssignment);
				assertEquals(0, possibleAssignments.size());
				
//				propertyAssignment.setPropertyValue("2013-08-18 19:16:00");
			} else {
				assertTrue(false);
			}
		}
		
		List<Action> toExecuteActions = newLinkedList();
		toExecuteActions.add(action);
		manipulator.execute(toExecuteActions, context);
		
		Individual purchaseOrder = modelOntology.getIndividual(context.getVariables().get(variableName));
		assertNotNull(purchaseOrder);
		
		Property purchaseRequestClientProperty = modelOntology.getProperty(purchaseRequestClientURI);
		RDFNode damianRDFNode = purchaseOrder.getPropertyValue(purchaseRequestClientProperty);
		
		assertNotNull(damianRDFNode);
		assertTrue(damianRDFNode.isResource());
		assertEquals(damianRDFNode.asResource().getURI(), damianURI);
		
		Property purchaseRequestResponsibleProperty = modelOntology.getProperty(purchaseRequestResponsibleURI);
		RDFNode employeeRDFNode = purchaseOrder.getPropertyValue(purchaseRequestResponsibleProperty);
		
		assertNotNull(employeeRDFNode);
		assertTrue(employeeRDFNode.isResource());
		assertEquals(employeeRDFNode.asResource().getURI(), employeeURI);
		
		Property commentProperty = modelOntology.getProperty(commentURI);
		RDFNode commentRDFNode = purchaseOrder.getPropertyValue(commentProperty);
		
		assertNotNull(commentRDFNode);
		assertTrue(commentRDFNode.isResource());
	}
	
	@Test
	public void testing() throws IOException, InvalidPropertyAssignment {
		String variableName = "purchaseOrder";
		OntModel bpmnOntology = getOntologyFromFile(bpmnOntologyPath);
		OntModel modelOntology = getOntologyFromFile(testingErrorPath);
		
		Property property = modelOntology.getProperty(commentURI);
		OntClass ontClass = modelOntology.getOntClass(purchaseRequestClassURI);
		Individual purchaseRequest = ontClass.createIndividual();
		assertNotNull(purchaseRequest);
		
		OntModel freshModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
		
		
		RDFNode oldPropertyValue = purchaseRequest.getPropertyValue(property);
		
		System.out.println(oldPropertyValue);
	}
}
