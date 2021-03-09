/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.opensearch.action.admin.indices.template.put;

import org.opensearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.MetadataIndexTemplateService;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;

public class TransportPutComposableIndexTemplateAction
    extends TransportMasterNodeAction<PutComposableIndexTemplateAction.Request, AcknowledgedResponse> {

    private final MetadataIndexTemplateService indexTemplateService;

    @Inject
    public TransportPutComposableIndexTemplateAction(TransportService transportService, ClusterService clusterService,
                                                     ThreadPool threadPool, MetadataIndexTemplateService indexTemplateService,
                                                     ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver) {
        super(PutComposableIndexTemplateAction.NAME, transportService, clusterService, threadPool, actionFilters,
            PutComposableIndexTemplateAction.Request::new, indexNameExpressionResolver);
        this.indexTemplateService = indexTemplateService;
    }

    @Override
    protected String executor() {
        // we go async right away
        return ThreadPool.Names.SAME;
    }

    @Override
    protected AcknowledgedResponse read(StreamInput in) throws IOException {
        return new AcknowledgedResponse(in);
    }

    @Override
    protected ClusterBlockException checkBlock(PutComposableIndexTemplateAction.Request request, ClusterState state) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.METADATA_WRITE);
    }

    @Override
    protected void masterOperation(final PutComposableIndexTemplateAction.Request request, final ClusterState state,
                                   final ActionListener<AcknowledgedResponse> listener) {
        ComposableIndexTemplate indexTemplate = request.indexTemplate();
        indexTemplateService.putIndexTemplateV2(request.cause(), request.create(), request.name(), request.masterNodeTimeout(),
            indexTemplate, listener);
    }
}