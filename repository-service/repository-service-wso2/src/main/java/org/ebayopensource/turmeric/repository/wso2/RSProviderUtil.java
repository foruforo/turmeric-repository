/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

package org.ebayopensource.turmeric.repository.wso2;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.ebayopensource.turmeric.services.common.error.RepositoryServiceErrorDescriptor;

import org.ebayopensource.turmeric.common.v1.types.*;
import org.ebayopensource.turmeric.repository.v2.services.*;

public class RSProviderUtil {
	private static final String __systemRoot = "/_system/governance/trunk";
	private static final String __artifactIdPropName = "registry.artifactId";
	public static String __artifactVersionPropName = "org.ebayopensource.turmeric.artifactVersion";

	private static final RSProviderUtil __instance = new RSProviderUtil();

	private Registry _registry;

	/**
	 * @throws IllegalStateException
	 */
	public RSProviderUtil() throws IllegalStateException {
		try {
			String username = System.getProperty("org.ebayopensource.turmeric.repository.wso2.username");
			String password = System.getProperty("org.ebayopensource.turmeric.repository.wso2.password");
			String url = System.getProperty("org.ebayopensource.turmeric.repository.wso2.url");
			Registry rootRegistry = new RemoteRegistry(new URL(url), username, password);
			_registry = GovernanceUtils.getGovernanceUserRegistry(rootRegistry, username);

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * @return
	 */
	public static final Registry getRegistry() {
		return __instance._registry;
	}

	/**
	 * @param collection
	 * @param libraries
	 * @throws Exception
	 */
	public static void findLibraries(Collection collection,
			Set<String> libraries) throws Exception {
		Registry wso2 = getRegistry();

		String[] children = collection.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				String child = children[i];
				Resource resource = wso2.get(child);

				if (resource instanceof org.wso2.carbon.registry.core.Collection) {
					findLibraries(
							(org.wso2.carbon.registry.core.Collection) resource,
							libraries);
				} else {
					// this collection contains a non collection resource, so it
					// is a library
					libraries.add(collection.getId());
				}
			}
		}
	}

	/**
	 * @param response
	 */
	public static <T extends BaseResponse> T setSuccessResponse(T response) {

		if (response != null)
			response.setAck(AckValue.SUCCESS);
		return response;
	}

	/**
	 * @param errors
	 * @param response
	 */
	public static <T extends BaseResponse> T addErrorsToResponse(
			List<CommonErrorData> errors, T response) {
		if (errors != null && !errors.isEmpty() && response != null) {
			if (response.getErrorMessage() == null) {
				response.setErrorMessage(new ErrorMessage());
			}
			response.getErrorMessage().getError().addAll(errors);
			response.setAck(AckValue.FAILURE);
		}
		return response;
	}

	/**
	 * @param exception
	 * @param response
	 */
	public static <T extends BaseResponse> T setExceptionMessageToResponse(
			Exception exception, T response) {
		if (response != null) {
			response.setAck(AckValue.FAILURE);

			if (response.getErrorMessage() == null) {
				ErrorMessage errorMessage = new ErrorMessage();
				response.setErrorMessage(errorMessage);
			}
			response.getErrorMessage().getError()
					.add(parseGeneralExceptionMessage(exception));
		}
		return response;
	}

	/**
	 * @author csubhash
	 * @param exception
	 * @return returns a message parsed error data object
	 */
	public static CommonErrorData parseGeneralExceptionMessage(
			Exception exception) {
		List<ErrorParameter> errorDataList = new ArrayList<ErrorParameter>();
		StackTraceElement[] stackTraceElements = exception.getStackTrace();
		int index = 0;

		ErrorParameter exceptionClassErrorParameter = new ErrorParameter();
		exceptionClassErrorParameter.setName("exceptionClass");
		exceptionClassErrorParameter.setValue(exception.getClass()
				.getCanonicalName());
		errorDataList.add(exceptionClassErrorParameter);

		ErrorParameter fileNameErrorParameter = new ErrorParameter();
		fileNameErrorParameter.setName("fileName");
		fileNameErrorParameter
				.setValue(stackTraceElements[index].getFileName());
		errorDataList.add(fileNameErrorParameter);

		ErrorParameter classNameErrorParameter = new ErrorParameter();
		classNameErrorParameter.setName("className");
		classNameErrorParameter.setValue(stackTraceElements[index]
				.getClassName());
		errorDataList.add(classNameErrorParameter);

		ErrorParameter methodNameErrorParameter = new ErrorParameter();
		methodNameErrorParameter.setName("methodName");
		methodNameErrorParameter.setValue(stackTraceElements[index]
				.getMethodName());
		errorDataList.add(methodNameErrorParameter);

		ErrorParameter lineNoErrorParameter = new ErrorParameter();
		lineNoErrorParameter.setName("lineNo");
		int linenum = stackTraceElements[index].getLineNumber();
		lineNoErrorParameter.setValue(Integer.valueOf(linenum).toString());
		errorDataList.add(lineNoErrorParameter);

		ErrorParameter messageErrorParameter = new ErrorParameter();
		messageErrorParameter.setName("errorMessage");
		messageErrorParameter.setValue(exception.getMessage());
		errorDataList.add(messageErrorParameter);

		CommonErrorData errorData = RepositoryServiceErrorDescriptor.UNKNOWN_EXCEPTION
				.newError(errorDataList);
		return errorData;
	}

	/**
	 * @param exception
	 * @param response
	 * @param errorDescriptor
	 */
	public static <T extends BaseResponse> T handleException(
			Exception exception, T response,
			RepositoryServiceErrorDescriptor errorDescriptor) {
		exception.printStackTrace();

		response.setAck(AckValue.FAILURE);
		ErrorParameter errorParameter = new ErrorParameter();
		errorParameter.setName("Error Message");
		errorParameter.setValue(exception.getMessage());
		List<CommonErrorData> errorDataList = new ArrayList<CommonErrorData>();
		errorDataList.add(errorDescriptor.newError(errorParameter));

		if (response.getErrorMessage() == null) {
			response.setErrorMessage(new ErrorMessage());
		}
		response.getErrorMessage().getError().addAll(errorDataList);

		return response;
	}

	/**
	 * Complete assetKey from a potentially partial key, assetType and
	 * assetVersion. Uses
	 * {@link #getAssetIdentifer(String, Library, String, String, List)}.
	 * 
	 * @param assetKey
	 * @param assetType
	 * @param assetVersion
	 * @return
	 */
	public static AssetKey completeAssetKey(AssetKey assetKey,
			String assetType, String assetVersion) {
		if (assetKey != null) {
			String assetId = assetKey.getAssetId();

			if (assetType == null && assetId != null) {
				assetType = getAssetType(assetId);
			}

			if (assetKey.getAssetName() == null) {
				assetKey.setAssetName(assetId.substring(assetId
						.lastIndexOf('/') + 1));
			}	
			
			if (assetId == null && assetKey.getAssetName() != null && assetVersion != null && assetType != null) {
				assetId = assetKey.getAssetName() + assetType + assetVersion;
				assetKey.setAssetId(assetId);
			}
			
			if (assetType != null) {
				assetKey.setType(assetType);
			}
			
			if (assetVersion != null) {
				assetKey.setVersion(assetVersion);
			}
			
		}

		return assetKey;
	}

	/**
	 * @param path
	 * @return A type derived from a path
	 */
	public static String getAssetType(String path) {
		String type = path;
		int systemRootLength = __systemRoot.length();
		if (type.startsWith(__systemRoot)) {
			type = Character.toUpperCase(type.charAt(systemRootLength+1))
					+ type.substring(systemRootLength + 2);
			type = type.substring(0, type.indexOf("/", 1));
			if (type.endsWith("s")) {
				type = type.substring(0, type.length() - 1);
			}

			int idx = 0;
			while ((idx = type.indexOf("_")) > 0) {
				type = type.substring(0, idx) + " "
						+ Character.toUpperCase(type.charAt(idx + 1))
						+ type.substring(idx + 2);
			}
		}
		return type;
	}

	/**
	 * @param info
	 * @param resource
	 * @param doc
	 * @return
	 */
	public static ExtendedAssetInfo completeExtendedAssetInfo(
			ExtendedAssetInfo info, Resource resource, Document doc) {
		List<AttributeNameValue> attributes = info.getAttribute();

		Properties properties = resource.getProperties();
		for (Object key : properties.keySet()) {
			Object value = properties.get(key);
			if (!(value instanceof List)) {
				AttributeNameValue attr = new AttributeNameValue();
				attr.setAttributeName(key.toString());
				attr.setAttributeValueString(value.toString());
				attributes.add(attr);
			} else {
				for (Object item : (List<?>) value) {
					if (item != null) {
						AttributeNameValue attr = new AttributeNameValue();
						attr.setAttributeName(key.toString());
						attr.setAttributeValueString(item.toString());
						attributes.add(attr);
					}
				}
			}
		}

		return info;
	}

	/**
	 * Updates the referenced resource object, so it can contain the properties
	 * of the ExtendedAssetInfo
	 * 
	 * @param resource
	 *            - {@link Resource} object to update
	 * @param info
	 *            - {@link ExtendedAssetInfo} object with the list
	 *            ofAttributeNameValue to set in the resource as properties
	 */
	public static void updateResourceProperties(Resource resource,
			ExtendedAssetInfo info) {
		List<AttributeNameValue> attributes = info.getAttribute();

		for (AttributeNameValue attrNameValue : attributes) {
			String attrName = attrNameValue.getAttributeName();
			if (attrName != null) {
				String attrValue = attrNameValue.getAttributeValueString();
				if (attrValue == null) {
					Long attrLong = attrNameValue.getAttributeValueLong();
					if (attrLong != null) {
						attrValue = attrLong.toString();
					} else {
						Boolean attrBool = attrNameValue
								.isAttributeValueBoolean();
						if (attrBool != null) {
							attrValue = attrBool.toString();
						}
					}
				}
				if (attrValue != null) {
					resource.setProperty(attrName, attrValue);
				}
			}
		}

		return;

	}

	/**
	 * Parse the contents of a Resource as XML
	 * 
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public static Document parseContent(Resource resource) throws Exception {

		Object content = resource.getContent();
		if (content == null)
			return null;

		if (resource.getMediaType().toLowerCase().indexOf("xml") < 0)
			return null;

		InputStream in = (content instanceof byte[]) ? new ByteArrayInputStream(
				(byte[]) content) : new StringInputStream(content.toString(),
				"utf-8");

		return DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(in);
	}

	/**
	 * @param info
	 * @param asset
	 * @param doc
	 * @throws XPathExpressionException
	 */
	public static <T extends BasicAssetInfo> T completeBasicAssetInfo(T info,
			Resource asset, Document doc) throws XPathExpressionException {
		if (doc == null) {
			info.setAssetDescription(asset.getDescription());
			info.setAssetName(getAssetName(asset.getPath()));
		} else {
			XPath xpath = XPathFactory.newInstance().newXPath();
			info.setAssetName(xpath.evaluate("/*/overview/name", doc));
			info.setAssetDescription(xpath.evaluate("/*/overview/description",
					doc));
		}

		info.setVersion(asset.getProperty(__artifactVersionPropName));
		info.setAssetType(getAssetType(asset.getPath()));

		return info;
	}

	private static String getAssetName(String path) {
		String assetName = path;
		if (assetName.startsWith("/_system/governance/")) {
			assetName = assetName.substring(assetName.lastIndexOf("/") + 1);
		}
		return assetName;
	}

	/**
	 * @param info
	 * @param service
	 * @param doc
	 * @return
	 * @throws XPathExpressionException
	 */
	public static AssetLifeCycleInfo completeAssetLifeCycleInfo(
			AssetLifeCycleInfo info, Resource asset, Document doc)
			throws XPathExpressionException {
		if (doc != null) {
			XPath xpath = XPathFactory.newInstance().newXPath();
			info.setDomainOwner(xpath.evaluate("/*/contacts/textContact", doc));
			info.setDomainType(xpath.evaluate("/*/contacts/contact", doc));
		}
		RSLifeCycle lifeCycle = RSLifeCycle.get(asset);
		info.setLifeCycleState(lifeCycle.getState());
		return info;
	}

	/**
	 * @param artifactList
	 * @param resource
	 * @throws RegistryException
	 */
	public static void retrieveAssociations(List<ArtifactInfo> artifactList,
			FlattenedRelationship relationship, Resource asset)
			throws RegistryException {
		Registry wso2 = RSProviderUtil.getRegistry();
		String assetId = asset.getPath();

		Association[] associations = wso2.getAllAssociations(assetId);
		if (associations != null) {
			for (Association assoc : associations) {
				final String srcPath = assoc.getSourcePath();
				final String dstPath = assoc.getDestinationPath();
				String type = assoc.getAssociationType();

				if (assetId.equals(srcPath)) {
					if (!wso2.resourceExists(dstPath)) {
						wso2.removeAssociation(srcPath, dstPath, type);
					} else if ("depends".equals(type)) {
						if (artifactList != null) {
							Resource other = wso2.get(dstPath);

							Artifact artifact = new Artifact();
							artifact.setArtifactName(dstPath.substring(dstPath
									.lastIndexOf('/') + 1));
							artifact.setArtifactIdentifier(dstPath);

							String assetType = getAssetType(dstPath);
							artifact.setArtifactCategory(assetType);

							if ("Endpoint".equals(assetType))
								artifact.setArtifactValueType(ArtifactValueType.URL);
							else
								artifact.setArtifactValueType(ArtifactValueType.FILE);

							ArtifactInfo artifactInfo = new ArtifactInfo();
							artifactInfo.setArtifact(artifact);
							artifactInfo.setArtifactDetail((byte[]) other
									.getContent());
							artifactInfo.setContentType(other.getMediaType());

							artifactList.add(artifactInfo);
						}
					} else {
						relationship.setDepth(1);
						List<Relation> relationList = relationship
								.getRelatedAsset();
						if (relationList != null) {
							Relation relation = new Relation();

							relation.setSourceAsset(completeAssetKey(
									new AssetKey() {
										{
											this.setAssetId(srcPath);
										}
									}, null, null));

							relation.setTargetAsset(completeAssetKey(
									new AssetKey() {
										{
											this.setAssetId(dstPath);
										}
									}, null, null));
							relation.setAssetRelationship(type);

							relationList.add(relation);
						}
					}
				}
			}
		}
	}

	private static Document createXmlDoc(String assetName,
			String assetDescription)
			throws ParserConfigurationException {
		// prepare xml document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation impl = builder.getDOMImplementation();

		Document doc = impl.createDocument(null, null, null);

		Element name = doc.createElementNS(
				"http://www.wso2.org/governance/metadata", "name");
		name.setTextContent(assetName);
		Element desc = doc.createElementNS(
				"http://www.wso2.org/governance/metadata", "description");
		desc.setTextContent(assetDescription);

		// <overview> element
		Element overview = doc.createElementNS(
				"http://www.wso2.org/governance/metadata", "overview");
		overview.appendChild(name);
		overview.appendChild(desc);

		// <serviceMetaData> element
		Element root = doc.createElementNS(
				"http://www.wso2.org/governance/metadata", "serviceMetaData");
		root.appendChild(overview);

		doc.appendChild(root);

		return doc;
	}

	private static void appendLifeCycleXml(Document doc,
			AssetLifeCycleInfo lifeCycleInfo) {
		Element contacts = doc.createElementNS(
				"http://www.wso2.org/governance/metadata", "contacts");
		String domainOwner = lifeCycleInfo.getDomainOwner();
		if (domainOwner != null && domainOwner.length() > 0) {
			Element textContact = doc.createElementNS(
					"http://www.wso2.org/governance/metadata", "textContact");
			textContact.setTextContent(domainOwner);
			contacts.appendChild(textContact);
		}

		String domainType = lifeCycleInfo.getDomainType();
		if (domainType != null && domainType.length() > 0) {
			Element contact = doc.createElementNS(
					"http://www.wso2.org/governance/metadata", "contact");
			contact.setTextContent(domainType);
			contacts.appendChild(contact);
		}

		if (contacts.hasChildNodes()) {
			Node root = doc.getFirstChild();
			root.appendChild(contacts);
		}
	}

	public static void createArtifacts(AssetKey parentKey,
			List<ArtifactInfo> artifactList) throws Exception {
		Registry wso2 = getRegistry();

		if (artifactList != null) {
			for (ArtifactInfo artifactInfo : artifactList) {
				Artifact artifact = artifactInfo.getArtifact();
				byte[] content = artifactInfo.getArtifactDetail();

				AssetKey artifactKey = new AssetKey();
				artifactKey.setAssetName(artifact.getArtifactName());
				completeAssetKey(artifactKey, artifact.getArtifactCategory(),
						null);

				String artifactId = artifactKey.getAssetId();
				Resource artifactRes = wso2.newResource();
				artifactRes.setProperty(__artifactIdPropName, UUID.randomUUID()
						.toString());
				artifactRes.setMediaType(artifactInfo.getContentType());
				if (artifact.getArtifactValueType() == ArtifactValueType.URL) {
					artifactRes.setContent(new String(content, "UTF-8"));
				} else {
					InputStream contentStream = new ByteArrayInputStream(
							content);
					artifactRes.setContentStream(contentStream);
				}
				wso2.put(artifactId, artifactRes);
				wso2.addAssociation(parentKey.getAssetId(),
						artifactKey.getAssetId(), "depends");
			}
		}
	}

	public static void updateArtifacts(AssetKey assetKey,
			List<ArtifactInfo> artifactList, List<ArtifactInfo> newArtifactList)
			throws Exception {
		HashMap<String, ArtifactInfo> artifactHash = new HashMap<String, ArtifactInfo>();

		if (artifactList != null) {
			for (ArtifactInfo artifactInfo : artifactList) {
				Artifact artifact = artifactInfo.getArtifact();
				if (artifact != null) {
					artifactHash.put(artifact.getArtifactIdentifier(),
							artifactInfo);
				}
			}
		}

		// now replace the old values with the new ones and add the new values
		if (newArtifactList != null) {
			for (ArtifactInfo newArtifactInfo : newArtifactList) {
				Artifact artifact = newArtifactInfo.getArtifact();
				if (artifact != null) {
					artifactHash.put(artifact.getArtifactIdentifier(),
							newArtifactInfo);
				}
			}
		}

		if (artifactHash.size() > 0) {
			createArtifacts(assetKey,
					new ArrayList<ArtifactInfo>(artifactHash.values()));
		}
	}

	public static void removeArtifacts(String assetId) throws Exception {
		Registry wso2 = getRegistry();
		List<String> assetsToDelete = new ArrayList<String>();
		String path = "/" + assetId;
		Association[] associations = wso2.getAllAssociations(path);
		if (associations != null) {
			for (Association assoc : associations) {
				String srcPath = assoc.getSourcePath();
				String dstPath = assoc.getDestinationPath();
				String type = assoc.getAssociationType();
				if ((assetId.equals(srcPath) && !dstPath.equals(path))
						&& ("depends".equals(type) || "usedBy".equals(type))) {
					wso2.removeAssociation(srcPath, dstPath, type);
					if (wso2.resourceExists(dstPath)) {
						assetsToDelete.add(dstPath);
					}
				}
			}
			// now i delete the artifacts interdependencies
			for (String assetToDelete : assetsToDelete) {
				Association[] artifactAssociations = wso2
						.getAllAssociations(assetToDelete);
				if (artifactAssociations != null) {
					for (Association artAssoc : artifactAssociations) {
						wso2.removeAssociation(artAssoc.getSourcePath(),
								artAssoc.getDestinationPath(),
								artAssoc.getAssociationType());
					}
				}
			}

			// now i delete the artifacts
			for (String assetToDelete : assetsToDelete) {
				wso2.delete(assetToDelete);
			}
		}
	}

	public static void createDependencies(AssetKey assetKey,
			List<Relation> relationList) throws Exception {
		if (relationList != null) {
			Registry wso2 = RSProviderUtil.getRegistry();

			String assetId = assetKey.getAssetId();
			for (Relation relation : relationList) {
				AssetKey srcAssetKey = RSProviderUtil.completeAssetKey(
						relation.getSourceAsset(), null, null);
				AssetKey dstAssetKey = RSProviderUtil.completeAssetKey(
						relation.getTargetAsset(), null, null);

				String srcAssetId = srcAssetKey.getAssetId();
				String dstAssetId = dstAssetKey.getAssetId();
				if (assetId.equals(srcAssetId)
						&& wso2.resourceExists(dstAssetId)) {
					wso2.addAssociation(srcAssetId, dstAssetId,
							relation.getAssetRelationship());
				}
			}
		}
	}

	public static void createDependencies(AssetKey assetKey,
			FlattenedRelationship relationship) throws Exception {
		if (relationship != null) {
			createDependencies(assetKey, relationship.getRelatedAsset());
		}
	}

	public static void updateDependencies(AssetKey assetKey,
			FlattenedRelationship origRelationship,
			FlattenedRelationshipForUpdate updateRelationship) throws Exception {
		HashMap<String, Relation> relationHash = new HashMap<String, Relation>();

		if (origRelationship != null) {
			for (Relation relation : origRelationship.getRelatedAsset()) {
				if (relation != null) {
					StringBuilder key = new StringBuilder();
					key.append(relation.getSourceAsset().getAssetId());
					key.append(relation.getTargetAsset().getAssetId());
					key.append(relation.getAssetRelationship());
					relationHash.put(key.toString(), relation);
				}
			}
		}

		if (updateRelationship != null) {
			String assetId = assetKey.getAssetId();
			for (RelationForUpdate relation : updateRelationship
					.getRelatedAsset()) {
				if (assetId.equals(relation.getCurrentSourceAsset()
						.getAssetId())) {
					Relation newRelation = relation.getNewRelation();
					if (newRelation != null) {
						StringBuilder key = new StringBuilder();
						key.append(newRelation.getSourceAsset().getAssetId());
						key.append(newRelation.getTargetAsset().getAssetId());
						key.append(newRelation.getAssetRelationship());
						relationHash.put(key.toString(), newRelation);
					}
				}
			}
		}

		if (relationHash.size() > 0) {
			createDependencies(assetKey,
					new ArrayList<Relation>(relationHash.values()));
		}
	}

	public static void removeDependencies(String assetId) throws Exception {
		Registry wso2 = getRegistry();

		Association[] relations = wso2.getAllAssociations(assetId);
		if (relations != null) {
			for (Association relationship : relations) {
				String srcPath = relationship.getSourcePath();
				String type = relationship.getAssociationType();
				if (assetId.equals(srcPath) && !"depends".equals(type)) {
					wso2.removeAssociation(srcPath,
							relationship.getDestinationPath(), type);
				}
			}
		}
	}

	public static void moveAsset(AssetKey oldAssetKey, AssetInfo assetInfo)
			throws Exception {
		BasicAssetInfo basicInfo = assetInfo.getBasicAssetInfo();
		AssetKey assetKey = basicInfo.getAssetKey();
		String assetId = assetKey.getAssetId();

		Registry wso2 = getRegistry();

		List<ArtifactInfo> artifactList = assetInfo.getArtifactInfo();
		for (ArtifactInfo artifactInfo : artifactList) {
			Artifact artifact = artifactInfo.getArtifact();
			if (!artifact.getArtifactValueType().equals(ArtifactValueType.URL)) {
				String artifactCategory = artifact.getArtifactCategory();

				AssetKey oldArtifactKey = new AssetKey();
				oldArtifactKey.setAssetName(artifact.getArtifactName());
				completeAssetKey(oldArtifactKey, artifactCategory, null);

				AssetKey newArtifactKey = new AssetKey();
				newArtifactKey.setAssetName(artifact.getArtifactName());
				completeAssetKey(newArtifactKey, artifactCategory, null);

				String oldArtifactId = oldArtifactKey.getAssetId();
				String newArtifactId = newArtifactKey.getAssetId();
				if (wso2.resourceExists(oldArtifactId)
						&& !newArtifactId.equals(oldArtifactId)
						&& !wso2.resourceExists(newArtifactId)) {
					wso2.move(oldArtifactId, newArtifactId);
				}
			}
		}

		wso2.move(oldAssetKey.getAssetId(), assetId);
	}

	public static String getXmlString(Document doc) throws Exception {
		// transform the Document into a String
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "2");

		StringWriter sw = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	public static AttributeNameValue newAttribute(String name, String value) {
		AttributeNameValue attr = new AttributeNameValue();
		attr.setAttributeName(name);
		attr.setAttributeValueString(value);
		return attr;
	}

	public static AttributeNameValue newAttribute(String name, long value) {
		AttributeNameValue attr = new AttributeNameValue();
		attr.setAttributeName(name);
		attr.setAttributeValueLong(value);
		return attr;
	}

	public static AttributeNameValue newAttribute(String name, boolean value) {
		AttributeNameValue attr = new AttributeNameValue();
		attr.setAttributeName(name);
		attr.setAttributeValueBoolean(value);
		return attr;
	}

	public static String getAttribute(ExtendedAssetInfo extendedInfo,
			String name, String dflt) {
		List<AttributeNameValue> attrs = extendedInfo.getAttribute();
		for (AttributeNameValue attr : attrs) {
			if (attr.getAttributeName().equalsIgnoreCase(name)) {
				return attr.getAttributeValueString();
			}
		}
		return dflt;
	}

	public static long getAttribute(ExtendedAssetInfo extendedInfo,
			String name, long dflt) {
		List<AttributeNameValue> attrs = extendedInfo.getAttribute();
		for (AttributeNameValue attr : attrs) {
			if (attr.getAttributeName().equalsIgnoreCase(name)) {
				return attr.getAttributeValueLong();
			}
		}
		return dflt;
	}

	public static AssetInfo getAssetInfo(AssetKey assetKey, Resource asset)
			throws Exception {
		Document doc = RSProviderUtil.parseContent(asset);

		if (doc != null) {
			XPath xpath = XPathFactory.newInstance().newXPath();

			if (Integer
					.parseInt(xpath.evaluate("count(/serviceMetaData)", doc)) == 0)
				return null;
		}

		// Create the basic service info structure
		BasicAssetInfo basicAssetInfo = new BasicAssetInfo();
		basicAssetInfo.setAssetKey(assetKey);
		RSProviderUtil.completeBasicAssetInfo(basicAssetInfo, asset, doc);

		// Create the extended service info structure
		ExtendedAssetInfo extendedAssetInfo = new ExtendedAssetInfo();
		RSProviderUtil.completeExtendedAssetInfo(extendedAssetInfo, asset, doc);

		// do the lifecycle stuff
		AssetLifeCycleInfo assetLifeCycleInfo = new AssetLifeCycleInfo();
		RSProviderUtil.completeAssetLifeCycleInfo(assetLifeCycleInfo, asset,
				doc);

		// create relationship info
		FlattenedRelationship relationship = new FlattenedRelationship();
		relationship.setDepth(0);
		relationship.setSourceAsset(assetKey);

		// populate asset info
		AssetInfo assetInfo = new AssetInfo();
		assetInfo.setBasicAssetInfo(basicAssetInfo);
		assetInfo.setExtendedAssetInfo(extendedAssetInfo);
		assetInfo.setAssetLifeCycleInfo(assetLifeCycleInfo);
		assetInfo.setFlattenedRelationship(relationship);

		RSProviderUtil.retrieveAssociations(assetInfo.getArtifactInfo(),
				relationship, asset);

		return assetInfo;
	}
	
	public static AssetInfo getAssetInfo(AssetKey assetKey, GovernanceArtifact asset)
			throws Exception {


		// Create the basic service info structure
		BasicAssetInfo basicAssetInfo = new BasicAssetInfo();
		basicAssetInfo.setAssetKey(assetKey);
		
		

		// Create the extended service info structure
		ExtendedAssetInfo extendedAssetInfo = new ExtendedAssetInfo();

		// do the lifecycle stuff
		AssetLifeCycleInfo assetLifeCycleInfo = new AssetLifeCycleInfo();

		// create relationship info
		FlattenedRelationship relationship = new FlattenedRelationship();
		relationship.setDepth(0);
		relationship.setSourceAsset(assetKey);

		// populate asset info
		AssetInfo assetInfo = new AssetInfo();
		assetInfo.setBasicAssetInfo(basicAssetInfo);
		assetInfo.setExtendedAssetInfo(extendedAssetInfo);
		assetInfo.setAssetLifeCycleInfo(assetLifeCycleInfo);
		assetInfo.setFlattenedRelationship(relationship);


		return assetInfo;
	}
	

	public static void lock(Resource service) {
		// TODO locking cannot be a boolean, needs to be a user.
		service.addProperty(
				"org.ebayopensource.turmeric.repository.wso2.locked", "true");
	}

	public static void unlock(Resource service) {
		service.addProperty(
				"org.ebayopensource.turmeric.repository.wso2.locked", "false");
	}

	public static boolean islocked(Resource service) {
		return Boolean
				.valueOf(service
						.getProperty("org.ebayopensource.turmeric.repository.wso2.locked"));
	}

	public static void updateExtendedInfo(ExtendedAssetInfo extendedInfo,
			ExtendedAssetInfo newExtendedInfo) {
		List<AttributeNameValue> attributes = extendedInfo.getAttribute();
		for (AttributeNameValue newAttr : newExtendedInfo.getAttribute()) {
			boolean found = false;
			for (AttributeNameValue attr : attributes) {
				if (attr.getAttributeName().equals(newAttr.getAttributeName())) {
					attr.setAttributeValueLong(newAttr.getAttributeValueLong());
					attr.setAttributeValueString(newAttr
							.getAttributeValueString());
					found = true;
					break;
				}
			}
			if (!found) {
				attributes.add(newAttr);
			}
		}

	}

	public static void updateLifeCycleInfo(AssetLifeCycleInfo lifeCycleInfo,
			AssetLifeCycleInfo newLifeCycleInfo) {
		if (newLifeCycleInfo.getApprover() != null) {
			lifeCycleInfo.setApprover(newLifeCycleInfo.getApprover());
		}

		if (newLifeCycleInfo.getArchitect() != null) {
			lifeCycleInfo.setArchitect(newLifeCycleInfo.getArchitect());
		}

		if (newLifeCycleInfo.getDomainOwner() != null) {
			lifeCycleInfo.setDomainOwner(newLifeCycleInfo.getDomainOwner());
		}

		if (newLifeCycleInfo.getDomainType() != null) {
			lifeCycleInfo.setDomainType(newLifeCycleInfo.getDomainType());
		}

		if (newLifeCycleInfo.getLifeCycleState() != null) {
			lifeCycleInfo.setLifeCycleState(newLifeCycleInfo
					.getLifeCycleState());
		}

		if (newLifeCycleInfo.getNextAction() != null) {
			lifeCycleInfo.setNextAction(newLifeCycleInfo.getNextAction());
		}

		if (newLifeCycleInfo.getOpsArchitect() != null) {
			lifeCycleInfo.setOpsArchitect(newLifeCycleInfo.getOpsArchitect());
		}

		if (newLifeCycleInfo.getProductManager() != null) {
			lifeCycleInfo.setProductManager(newLifeCycleInfo
					.getProductManager());
		}

		if (newLifeCycleInfo.getProjectManager() != null) {
			lifeCycleInfo.setProjectManager(newLifeCycleInfo
					.getProjectManager());
		}

		if (newLifeCycleInfo.getServiceArchitect() != null) {
			lifeCycleInfo.setServiceArchitect(newLifeCycleInfo
					.getServiceArchitect());
		}

		if (newLifeCycleInfo.getTraceTicket() != null) {
			lifeCycleInfo.setTraceTicket(newLifeCycleInfo.getTraceTicket());
		}

		if (newLifeCycleInfo.getTrackerId() != null) {
			lifeCycleInfo.setTrackerId(newLifeCycleInfo.getTrackerId());
		}
	}

	public static Resource newAssetResource() throws RegistryException {
		Resource asset = __instance._registry.newResource();
		RSLifeCycle.init(asset);

		RSProviderUtil.lock(asset);
		asset.setProperty(__artifactIdPropName, UUID.randomUUID().toString());
		return asset;
	}
	

	public static String getBasicAssetInfoXml(BasicAssetInfo basicInfo)
			throws Exception {
		Document doc = createXmlDoc(basicInfo.getAssetName(),
				basicInfo.getAssetDescription());

		return getXmlString(doc);
	}

	public static String getAssetInfoXml(AssetInfo assetInfo) throws Exception {
		BasicAssetInfo basicInfo = assetInfo.getBasicAssetInfo();

		Document doc = createXmlDoc(basicInfo.getAssetName(),
				basicInfo.getAssetDescription());

		appendLifeCycleXml(doc, assetInfo.getAssetLifeCycleInfo());

		return getXmlString(doc);
	}

	public static String getBasicServiceInfoXml(BasicServiceInfo basicInfo)
			throws Exception {
		Document doc = createXmlDoc(basicInfo.getServiceName(),
				basicInfo.getServiceDescription());

		return getXmlString(doc);
	}	
}
